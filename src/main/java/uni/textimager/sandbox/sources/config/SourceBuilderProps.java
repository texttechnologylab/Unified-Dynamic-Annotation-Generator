package uni.textimager.sandbox.sources.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.source-builder.enabled")
public record SourceBuilderProps(boolean enabled) {
}
