package uni.textimager.sandbox.importer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "duui.importer")
public record DUUIImporterProps(boolean enabled) {
}
