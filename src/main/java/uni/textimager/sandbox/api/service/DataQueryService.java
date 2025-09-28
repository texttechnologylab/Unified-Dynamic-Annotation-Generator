package uni.textimager.sandbox.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uni.textimager.sandbox.api.DummyDataProvider;
import uni.textimager.sandbox.api.ValueMode;
import uni.textimager.sandbox.api.charts.ChartRegistry;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataQueryService {

    private final ObjectMapper mapper;
    private final DummyDataProvider provider;
    private final ChartRegistry charts;

    public DataQueryService(ObjectMapper mapper,
                            DummyDataProvider provider,
                            ChartRegistry charts) {
        this.mapper = mapper;
        this.provider = provider;
        this.charts = charts;
    }

    public String buildArrayJson(String id, String type,
                                 Map<String, String> filters,
                                 Map<String, String> corpus,
                                 boolean pretty) {

        Set<String> files = Optional.ofNullable(corpus)
                .map(m -> m.get("files"))
                .map(Parsing::parseCsvSet)
                .orElseGet(Collections::emptySet);

        ValueMode vm = ValueMode.from(filters.get("valueMode"));
        filters.remove("valueMode");

        // Prefer handler if present
        if (charts.has(type)) {
            JsonNode node = charts.get(type).render(id, filters, files, vm);
            try {
                return pretty ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)
                        : mapper.writeValueAsString(node);
            } catch (Exception e) {
                return "[]";
            }
        }

        // fallback to your legacy provider paths if no handler found
        return provider.getJsonFor(id, type);
    }

    // -------- parsing utils used by legacy filters --------
    static final class Parsing {
        static Set<String> parseCsvSet(String csv) {
            if (csv == null || csv.isBlank()) return Collections.emptySet();
            return Arrays.stream(csv.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }
}
