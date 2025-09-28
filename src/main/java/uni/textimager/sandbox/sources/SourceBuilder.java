package uni.textimager.sandbox.sources;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.api.service.SourceBuildService;

@Component
@ConditionalOnProperty(name = "app.source-builder.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SourceBuilder implements ApplicationRunner {
    private final SourceBuildService service;

    @Override
    public void run(ApplicationArguments args) {
        service.startBuild(null); // or pass a property-driven path
    }
}
