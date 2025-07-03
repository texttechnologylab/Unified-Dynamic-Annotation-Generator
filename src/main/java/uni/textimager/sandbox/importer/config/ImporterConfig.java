package uni.textimager.sandbox.importer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uni.textimager.sandbox.importer.dialect.SqlDialect;
import uni.textimager.sandbox.importer.service.DataInserterService;
import uni.textimager.sandbox.importer.service.NameSanitizer;
import uni.textimager.sandbox.importer.service.SchemaGeneratorService;
import uni.textimager.sandbox.importer.service.XmiParserService;

import javax.xml.parsers.DocumentBuilderFactory;

@Configuration
public class ImporterConfig {
    @Bean
    public DocumentBuilderFactory documentBuilderFactory() {
        return DocumentBuilderFactory.newInstance();
    }

    @Bean
    public NameSanitizer nameSanitizer() {
        return new NameSanitizer();
    }

    @Bean
    public XmiParserService xmiParserService(NameSanitizer nameSanitizer) {
        return new XmiParserService(nameSanitizer);
    }

    @Bean
    public SchemaGeneratorService schemaGeneratorService(JdbcTemplate jdbcTemplate, SqlDialect sqlDialect) {
        return new SchemaGeneratorService(jdbcTemplate, sqlDialect);
    }

    @Bean
    public DataInserterService dataInserterService(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new DataInserterService(namedParameterJdbcTemplate);
    }
}
