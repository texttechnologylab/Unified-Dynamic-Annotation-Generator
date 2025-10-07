package uni.textimager.sandbox.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.*;

@Component
@ConditionalOnProperty(name = "app.pipeline-json-import.enabled", havingValue = "true")
public class PipelineJsonImporter implements ApplicationRunner {

    private static final String TABLE = "pipeline";
    private static final String COL_NAME = "pipeline_name";
    private static final String COL_JSON = "json";
    private static final String PIPELINE_ID = "pipeline_id";

    private final DataSource dataSource;
    private final Path folder;
    private final boolean replaceIfDifferent;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.db.schema:public}")
    private String schema;

    public PipelineJsonImporter(
            DataSource dataSource,
            @Value("${app.pipeline-json-import.folder:pipelines}") String folderPath,
            @Value("${app.pipeline-json-import.replace-if-different:false}") boolean replaceIfDifferent
    ) {
        this.dataSource = dataSource;
        this.folder = Paths.get(folderPath);
        this.replaceIfDifferent = replaceIfDifferent;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            System.out.println("[PipelineJsonImporter] Folder missing or not a directory: " + folder.toAbsolutePath());
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            // 1) Ensure schema exists (Postgres)
            dsl.execute("CREATE SCHEMA IF NOT EXISTS " + schema);

            // 2) Create table in that schema (PRIMARY KEY on id)
            dsl.createTableIfNotExists(name(schema, TABLE))
                    .column(name(COL_NAME), SQLDataType.VARCHAR(255).nullable(false))
                    .column(name(COL_JSON), SQLDataType.CLOB.nullable(false))
                    .column(name(PIPELINE_ID), SQLDataType.VARCHAR(255).nullable(false))
                    .constraints(constraint("PK_" + TABLE).primaryKey(name(PIPELINE_ID)))
                    .execute();

            System.out.printf("[PipelineJsonImporter] Ensured %s.%s exists%n", schema, TABLE);

            // 3) Import files
            try (Stream<Path> files = Files.list(folder)) {
                files.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".json"))
                        .forEach(p -> importOne(dsl, p));
            }
        }
    }

    private void importOne(DSLContext dsl, Path p) {
        try {
            String raw = Files.readString(p, StandardCharsets.UTF_8);
            ParsedPipeline parsed = parseAndCanonicalize(raw);

            String canonicalJson = parsed.canonicalJson();
            String pipelineIdOriginal = parsed.pipelineId();
            String pipelineName = filenameWithoutExt(p.getFileName().toString());

            if (pipelineNameExists(dsl, pipelineName)) {
                System.out.printf("[PipelineJsonImporter] Skipped (filename already imported) file=%s%n", pipelineName);
                return;
            }

            boolean idExists = pipelineIdExists(dsl, pipelineIdOriginal);

            if (!idExists) {
                // Either insert new, or (if replace==true and the same id exists) update — but it doesn't exist, so insert.
                dsl.insertInto(table(TABLE),
                                field(COL_NAME),
                                field(COL_JSON),
                                field(PIPELINE_ID))
                        .values(pipelineName, canonicalJson, pipelineIdOriginal)
                        .execute();
                System.out.printf("[PipelineJsonImporter] Inserted (id=%s, file=%s)%n", pipelineIdOriginal, pipelineName);
                return;
            }

            // Same id already exists
            if (replaceIfDifferent) {
                // Update the existing row with SAME id
                String existingCanonical = dsl.select(field(COL_JSON, String.class))
                        .from(table(TABLE))
                        .where(field(PIPELINE_ID).eq(pipelineIdOriginal))
                        .fetchOneInto(String.class);

                String existingCanon = (existingCanonical == null) ? null : canonicalize(existingCanonical);
                String newCanon = canonicalize(canonicalJson);

                if (existingCanon != null && existingCanon.equals(newCanon)) {
                    System.out.printf("[PipelineJsonImporter] Skipped (unchanged) id=%s%n", pipelineIdOriginal);
                    return;
                }

                int updated = dsl.update(table(TABLE))
                        .set(field(COL_JSON), canonicalJson)
                        .set(field(COL_NAME), pipelineName) // optional: keep last filename seen
                        .where(field(PIPELINE_ID).eq(pipelineIdOriginal))
                        .execute();
                System.out.printf("[PipelineJsonImporter] %s id=%s (updated from file=%s)%n",
                        updated == 1 ? "Updated" : "No update for", pipelineIdOriginal, pipelineName);
                return;
            }

            // replace-if-different = false → generate a new unique id with numeric prefix and INSERT
            String uniqueId = ensureUniquePipelineId(dsl, pipelineIdOriginal);
            dsl.insertInto(table(TABLE),
                            field(COL_NAME),
                            field(COL_JSON),
                            field(PIPELINE_ID))
                    .values(pipelineName, canonicalJson, uniqueId)
                    .execute();
            System.out.printf("[PipelineJsonImporter] Inserted duplicate as id=%s (original=%s, file=%s)%n",
                    uniqueId, pipelineIdOriginal, pipelineName);

        } catch (Exception e) {
            System.err.printf("[PipelineJsonImporter] Failed for %s: %s%n", p.getFileName(), e.getMessage());
        }
    }

// --- Helpers ---------------------------------------------------------------

    /**
     * Parse JSON once, return canonical string and extracted id (old & new format).
     */
    private ParsedPipeline parseAndCanonicalize(String raw) throws Exception {
        JsonNode root = mapper.readTree(raw);            // validate + parse
        String pipelineId = extractPipelineId(root);     // throws if missing
        String canonical = mapper.writeValueAsString(root);
        return new ParsedPipeline(canonical, pipelineId);
    }

    /**
     * Extracts the pipeline id from either {"pipelines":[{...}]} or single-object {...}.
     */
    private String extractPipelineId(JsonNode root) {
        JsonNode pipelineNode = root;
        if (root.has("pipelines")) {
            JsonNode arr = root.get("pipelines");
            if (!arr.isArray() || arr.isEmpty() || !arr.get(0).isObject()) {
                throw new IllegalArgumentException("Invalid pipeline JSON: expected non-empty array at \"pipelines\".");
            }
            pipelineNode = arr.get(0);
        }
        JsonNode idNode = pipelineNode.get("id");
        if (idNode == null || !idNode.isTextual() || idNode.asText().isBlank()) {
            throw new IllegalArgumentException("Invalid pipeline JSON: missing textual \"id\".");
        }
        return idNode.asText();
    }

    /**
     * Check if a pipeline_id already exists.
     */
    private boolean pipelineIdExists(DSLContext dsl, String pipelineId) {
        String hit = dsl.select(field(PIPELINE_ID, String.class))
                .from(table(TABLE))
                .where(field(PIPELINE_ID).eq(pipelineId))
                .fetchOneInto(String.class);
        return hit != null;
    }

    private boolean pipelineNameExists(DSLContext dsl, String pipelineName) {
        return dsl.fetchExists(
                selectOne()
                        .from(table(name(schema, TABLE)))
                        .where(field(name(COL_NAME)).eq(pipelineName))
        );
    }

    /**
     * If 'id' exists, returns "id-2", "id-3", ... until free.
     * If the given id already ends with "-N", continue from N+1.
     * Ensures <= 255 chars by truncating the BASE (left part) to fit the suffix.
     */
    private String ensureUniquePipelineId(DSLContext dsl, String id) {
        if (!pipelineIdExists(dsl, id)) return id;

        final int maxLen = 255;

        // If id already ends with -number, keep its base and bump the number
        String base = id;
        int counter = 2;
        Matcher m = Pattern.compile("^(.*?)-(\\d+)$").matcher(id);
        if (m.matches()) {
            base = m.group(1);
            try {
                counter = Integer.parseInt(m.group(2)) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        while (true) {
            String suffix = "-" + counter;

            // How many chars can the base keep?
            int keep = Math.max(1, maxLen - suffix.length()); // keep at least 1 char for base
            String trimmedBase = base.length() > keep ? base.substring(0, keep) : base;

            String candidate = trimmedBase + suffix;

            if (!pipelineIdExists(dsl, candidate)) {
                return candidate;
            }
            counter++;
        }
    }

    private String filenameWithoutExt(String name) {
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }

    private String canonicalize(String json) throws Exception {
        JsonNode node = mapper.readTree(json);   // validate + parse
        return mapper.writeValueAsString(node);  // minified canonical form
    }

    /**
     * Parsed result container
     */
    private record ParsedPipeline(String canonicalJson, String pipelineId) {
    }
}
