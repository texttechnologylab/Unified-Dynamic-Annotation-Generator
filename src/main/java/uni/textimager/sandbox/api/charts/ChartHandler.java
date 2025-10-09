// /charts/ChartHandler.java
package uni.textimager.sandbox.api.charts;

import com.fasterxml.jackson.databind.JsonNode;
import uni.textimager.sandbox.api.ValueMode;

import java.util.Map;
import java.util.Set;

public interface ChartHandler {
    JsonNode render(String generatorId,
                    Map<String, String> filters,
                    Set<String> files,
                    ValueMode valueMode,
                    String schema);
}
