package uni.textimager.sandbox.api.charts;

import com.fasterxml.jackson.databind.JsonNode;
import uni.textimager.sandbox.api.Handler.ValueMode;

import java.util.Map;
import java.util.Set;

public interface ChartHandler {
    /**
     * Key used for routing, e.g. "bar-chart", "pie-chart", "text", "line-chart", "map-2d", "network-2d".
     */
    String getName();

    JsonNode render(String generatorId,
                    Map<String, String> chartFilters,
                    Set<String> corpusFiles,
                    ValueMode valueMode);
}
