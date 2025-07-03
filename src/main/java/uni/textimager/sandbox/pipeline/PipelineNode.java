package uni.textimager.sandbox.pipeline;

import lombok.Getter;
import java.util.Map;

public class PipelineNode {
    @Getter
    private final PipelineNodeType type;
    @Getter
    private final Map<String, PipelineNode> dependencies;
    @Getter
    private final JSONView config;

    public PipelineNode(PipelineNodeType type, Map<String, PipelineNode> dependencies, JSONView config) {
        this.type = type;
        this.dependencies = dependencies;
        this.config = config;
    }
}
