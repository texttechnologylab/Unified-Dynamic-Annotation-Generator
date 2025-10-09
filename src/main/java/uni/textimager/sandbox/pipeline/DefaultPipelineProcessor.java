package uni.textimager.sandbox.pipeline;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.api.service.SourceBuildService;

/**
 * Default processor: loads the pipeline, runs it.
 */
@Component
public class DefaultPipelineProcessor implements PipelineProcessor {

    private final SourceBuildService sourceBuildService;

    private final Logger logger = LoggerFactory.getLogger(DefaultPipelineProcessor.class);

    public DefaultPipelineProcessor(SourceBuildService sourceBuildService) {
        this.sourceBuildService = sourceBuildService;
    }

    @Override
    public void process(String pipelineId) {
        logger.info("Processing pipeline " + pipelineId + " by running a source build.");
        sourceBuildService.startBuild(pipelineId, pipelineId);
    }
}
