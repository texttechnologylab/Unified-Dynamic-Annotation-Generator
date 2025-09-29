package uni.textimager.sandbox.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record UpdatePipelineRequest(
        JsonNode json
) {}
