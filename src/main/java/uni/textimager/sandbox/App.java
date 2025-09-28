package uni.textimager.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uni.textimager.sandbox.importer.config.DUUIImporterProps;
import uni.textimager.sandbox.importer.config.DbProps;
import uni.textimager.sandbox.sources.config.SourceBuilderProps;

@SpringBootApplication
@EnableConfigurationProperties({DbProps.class, DUUIImporterProps.class, SourceBuilderProps.class})
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
