package uni.textimager.sandbox.importer.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uni.textimager.sandbox.importer.dialect.SqlDialect;

import java.util.Map;

@Service
public class SchemaGeneratorService {
    private final JdbcTemplate jdbcTemplate;
    private final SqlDialect dialect;

    public SchemaGeneratorService(JdbcTemplate jdbcTemplate, SqlDialect dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.dialect = dialect;
    }

    public void generateSchema(Map<String, Map<String, Integer>> maxLengths) {
        maxLengths.forEach((entity, lengths) -> {
            String pk = entity.toLowerCase() + "_pk_id";
            StringBuilder ddl = new StringBuilder()
                    .append("CREATE TABLE IF NOT EXISTS ").append(entity).append(" (\n")
                    .append("  ").append(dialect.autoIncrementPrimaryKey(pk));
            lengths.forEach((col, max) -> {
                String columnName = entity.toUpperCase() + "_" + col.toUpperCase().replace(":", "_");
                String type = max > 255 ? dialect.clobType() : dialect.varcharType(Math.max(max, 1));
                ddl.append(",\n  ").append(columnName).append(" ").append(type);
            });
            ddl.append(",\n  PRIMARY KEY(").append(pk).append(")\n)");
            jdbcTemplate.execute(ddl.toString());
        });
    }
}
