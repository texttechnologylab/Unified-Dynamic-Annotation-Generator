package uni.textimager.sandbox.pipeline;

import lombok.Getter;

import java.util.Map;

@Getter
public class PipelineNode {
    private final PipelineNodeType type;
    private final Map<String, PipelineNode> dependencies;
    private final JSONView config;

    public PipelineNode(PipelineNodeType type, Map<String, PipelineNode> dependencies, JSONView config) {
        this.type = type;
        this.dependencies = dependencies;
        this.config = config;
    }
}
