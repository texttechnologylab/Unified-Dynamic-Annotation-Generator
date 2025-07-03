// src/main/java/uni/textimager/sandbox/importer/DatabaseGenerator.java
package uni.textimager.sandbox.importer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.importer.dialect.SqlDialect;
import uni.textimager.sandbox.importer.service.DataInserterService;
import uni.textimager.sandbox.importer.service.NameSanitizer;
import uni.textimager.sandbox.importer.service.SchemaGeneratorService;
import uni.textimager.sandbox.importer.service.XmiParserService;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseGenerator implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final SqlDialect dialect;
    private final XmiParserService parser;
    private final NameSanitizer sanitizer;
    private final SchemaGeneratorService schemaGen;
    private final DataInserterService dataInserter;

    @Value("${app.input-dir:src/main/resources/input}")
    private String inputDir;

    public DatabaseGenerator(JdbcTemplate jdbc,
                             SqlDialect dialect,
                             XmiParserService parser,
                             NameSanitizer sanitizer,
                             SchemaGeneratorService schemaGen,
                             DataInserterService dataInserter) {
        this.jdbc = jdbc;
        this.dialect = dialect;
        this.parser = parser;
        this.sanitizer = sanitizer;
        this.schemaGen = schemaGen;
        this.dataInserter = dataInserter;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, Map<String, Integer>> maxLengths = new LinkedHashMap<>();
        List<EntityRecord> allRecords = new ArrayList<>();

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Path.of(inputDir), "*.xmi")) {
            for (Path file : ds) {
                String fn = file.getFileName().toString();
                for (EntityRecord rec : parser.parse(file)) {
                    String table = sanitizer.toClassName(rec.tag());
                    maxLengths.computeIfAbsent(table, k -> new HashMap<>())
                            .merge("filename", fn.length(), Math::max);
                    rec.attributes().forEach((raw, val) ->
                            maxLengths.get(table)
                                    .merge(sanitizer.sanitize(raw), val.length(), Math::max)
                    );
                    allRecords.add(rec);
                }
            }
        }

        schemaGen.generateSchema(maxLengths);
        dataInserter.insertRecords(allRecords);

        // Insert metadata about tables
        String metaTable = "TableNames";
        String metaPk = "tablenames_id";
        String ddlMeta = "CREATE TABLE IF NOT EXISTS " + metaTable + " ("
                + dialect.autoIncrementPrimaryKey(metaPk) + ", "
                + "table_name " + dialect.varcharType(255) + ", "
                + "PRIMARY KEY(" + metaPk + "))";
        jdbc.execute(ddlMeta);

        String insertMeta = "INSERT INTO " + metaTable + "(table_name) VALUES(?)";
        List<Object[]> batchArgs = maxLengths.keySet().stream()
                .map(name -> new Object[]{name})
                .toList();
        jdbc.batchUpdate(insertMeta, batchArgs);
    }
}
