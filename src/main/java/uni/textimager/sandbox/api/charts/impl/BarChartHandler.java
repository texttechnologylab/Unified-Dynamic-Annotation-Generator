package uni.textimager.sandbox.api.charts.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.api.Handler.ValueMode;
import uni.textimager.sandbox.api.Repositories.GeneratorDataRepository;
import uni.textimager.sandbox.api.charts.ChartHandler;
import uni.textimager.sandbox.api.charts.ValueTransforms;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class BarChartHandler implements ChartHandler {

    private final ObjectMapper mapper;
    private final GeneratorDataRepository repo;

    public BarChartHandler(ObjectMapper mapper, GeneratorDataRepository repo) {
        this.mapper = mapper;
        this.repo = repo;
    }

    // small local utils
    private static Set<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return Arrays.stream(csv.split(",")).map(String::trim)
                .filter(s -> !s.isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Integer parseInt(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Double parseDouble(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "BarChart";
    }

    @Override
    public JsonNode render(String generatorId,
                           Map<String, String> filters,
                           Set<String> corpusFiles,
                           ValueMode valueMode) {
        // attrs
        LinkedHashSet<String> attrs = Arrays.stream(
                        Optional.ofNullable(filters.get("attrs")).orElse("").split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (attrs.isEmpty()) attrs = new LinkedHashSet<>(List.of("label", "value", "color"));

        // filter params
        Set<String> keepLabels = parseCsv(filters.getOrDefault("label", filters.getOrDefault("labels", "")));
        Double min = parseDouble(filters.get("min"));
        Double max = parseDouble(filters.get("max"));
        String sort = Optional.ofNullable(filters.get("sort")).orElse("value");
        boolean desc = Optional.ofNullable(filters.get("desc"))
                .map(v -> v.equalsIgnoreCase("true") || v.equals("1")).orElse(true);
        Integer limit = parseInt(filters.get("limit"));

        // RAW: DB does min/max/sort/limit; else: pull full, transform client-side
        List<GeneratorDataRepository.BarPieRow> rows =
                (valueMode == ValueMode.RAW)
                        ? repo.loadBarPie(generatorId, keepLabels, corpusFiles, min, max, sort, desc, limit)
                        : repo.loadBarPie(generatorId, keepLabels, corpusFiles, null, null, "value", true, null);

        if (valueMode != ValueMode.RAW) {
            rows = ValueTransforms.apply(rows, valueMode, repo, generatorId, keepLabels, corpusFiles);
            rows = ValueTransforms.sortLimitFilter(rows, sort, desc, min, max, limit);
        }

        ArrayNode out = mapper.createArrayNode();
        for (var r : rows) {
            ObjectNode o = mapper.createObjectNode();
            if (attrs.contains("label")) o.put("label", r.label());
            if (attrs.contains("value")) o.put("value", r.value());
            if (attrs.contains("color")) o.put("color", r.color() == null ? "#999999" : r.color());
            out.add(o);
        }
        return out;
    }
}
