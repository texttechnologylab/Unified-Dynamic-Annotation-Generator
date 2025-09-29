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
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "app.pipeline-json-import.enabled", havingValue = "true")
public class PipelineJsonImporter implements ApplicationRunner {

    private static final String TABLE = "pipeline";
    private static final String COL_NAME = "pipeline_name";
    private static final String COL_JSON = "json";

    private final DataSource dataSource;
    private final Path folder;
    private final boolean replaceIfDifferent;
    private final ObjectMapper mapper = new ObjectMapper();

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

            dsl.createTableIfNotExists(TABLE)
                    .column(COL_NAME, SQLDataType.VARCHAR(255).nullable(false))
                    .column(COL_JSON, SQLDataType.CLOB.nullable(false)) // switch to JSONB if on Postgres
                    .constraints(DSL.constraint("PK_" + TABLE).primaryKey(COL_NAME))
                    .execute();

            try (Stream<Path> files = Files.list(folder)) {
                files.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".json"))
                        .forEach(p -> importOne(dsl, p));
            }
        }
    }

    private void importOne(DSLContext dsl, Path p) {
        try {
            String raw = Files.readString(p, StandardCharsets.UTF_8);
            String canonicalJson = canonicalize(raw);
            String pipelineName = filenameWithoutExt(p.getFileName().toString());

            String existing = dsl.select(DSL.field(COL_JSON, String.class))
                    .from(DSL.table(TABLE))
                    .where(DSL.field(COL_NAME).eq(pipelineName))
                    .fetchOneInto(String.class);

            if (existing == null) {
                dsl.insertInto(DSL.table(TABLE),
                                DSL.field(COL_NAME),
                                DSL.field(COL_JSON))
                        .values(pipelineName, canonicalJson)
                        .execute();
                System.out.printf("[PipelineJsonImporter] Inserted %s%n", pipelineName);
                return;
            }

            String existingCanonical = canonicalize(existing);
            if (existingCanonical.equals(canonicalJson)) {
                System.out.printf("[PipelineJsonImporter] Skipped (unchanged) %s%n", pipelineName);
                return;
            }

            if (replaceIfDifferent) {
                int updated = dsl.update(DSL.table(TABLE))
                        .set(DSL.field(COL_JSON), canonicalJson)
                        .where(DSL.field(COL_NAME).eq(pipelineName))
                        .execute();
                System.out.printf("[PipelineJsonImporter] %s %s%n",
                        updated == 1 ? "Updated" : "No update for", pipelineName);
            } else {
                System.out.printf("[PipelineJsonImporter] Skipped (different exists; replace-if-different=false) %s%n",
                        pipelineName);
            }

        } catch (Exception e) {
            System.err.printf("[PipelineJsonImporter] Failed for %s: %s%n", p.getFileName(), e.getMessage());
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
}
