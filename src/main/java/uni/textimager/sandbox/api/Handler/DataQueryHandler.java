package uni.textimager.sandbox.api.Handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import uni.textimager.sandbox.api.DummyDataProvider;
import uni.textimager.sandbox.api.Repositories.GeneratorDataRepository;

import java.util.*;
import java.util.stream.Collectors;

import static uni.textimager.sandbox.api.Handler.DataQueryHandler.Parsing.*;

@Service
public class DataQueryHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final DummyDataProvider provider; // kept for non bar/pie
    private final GeneratorDataRepository repo;

    public DataQueryHandler(DummyDataProvider provider, GeneratorDataRepository repo) {
        this.provider = provider;
        this.repo = repo;
    }

    public String buildArrayJson(String id,
                                 String type,
                                 String attrsCsv,
                                 Map<String, String> filters,
                                 boolean pretty) {

        // Prefer explicit type; fall back to detection only for non-DB shapes
        String shape = normalizedShape(type, id);

        JsonNode result;
        if ("bar-chart".equals(shape) || "pie-chart".equals(shape)) {
            // DB-backed path for bar/pie. `id` is treated as GENERATORID.
            System.out.println(filters);
            result = dbBarPie(id, attrsCsv, filters);
        } else {
            // Fallback to existing dummy/provider for other shapes
            String raw = provider.getJsonFor(id, type);
            final JsonNode root;
            try {
                root = mapper.readTree(raw);
            } catch (Exception e) {
                return raw;
            }

            switch (detectShape(id, root)) {
                case "line-chart" -> result = filterLineChart(root, filters);
                case "map-2d" -> result = filterMap2D(root, filters);
                case "network-2d" -> result = filterNetwork2D(root, filters);
                case "text" -> result = root;
                default -> result = root;
            }
        }

        try {
            return pretty
                    ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result)
                    : mapper.writeValueAsString(result);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String normalizedShape(String type, String id) {
        if (type != null) {
            String t = type.trim().toLowerCase(Locale.ROOT);
            if (Set.of("bar-chart", "pie-chart", "line-chart", "map-2d", "network-2d", "text").contains(t)) return t;
        }
        if (id != null) {
            String i = id.trim().toLowerCase(Locale.ROOT);
            if (Set.of("bar-chart", "pie-chart", "line-chart", "map-2d", "network-2d", "text").contains(i)) return i;
        }
        return "bar-chart"; // default for DB aggregate
    }

    // ---------- DB bar/pie ----------
    private JsonNode dbBarPie(String generatorId,
                              String attrsCsv,
                              Map<String, String> filters) {

        // attrs default
        LinkedHashSet<String> attrs = Arrays.stream(Optional.ofNullable(attrsCsv).orElse("")
                        .split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (attrs.isEmpty()) attrs = new LinkedHashSet<>(List.of("label", "value", "color"));

        // filters -> DB
        Set<String> keepLabels = parseCsvSet(filters.get("label"));
        Double min = parseDouble(filters.get("min"));
        Double max = parseDouble(filters.get("max"));
        String sortKey = Optional.ofNullable(filters.get("sort")).orElse("value");
        boolean desc = Optional.ofNullable(filters.get("desc"))
                .map(v -> v.equalsIgnoreCase("true") || v.equals("1"))
                .orElse(true);
        Integer limit = parseInt(filters.get("limit"));

        // query DB
        List<GeneratorDataRepository.BarPieRow> rows =
                repo.loadBarPie(generatorId, keepLabels, min, max, sortKey, desc, limit);

        // projection
        ArrayNode out = mapper.createArrayNode();
        for (GeneratorDataRepository.BarPieRow r : rows) {
            ObjectNode o = mapper.createObjectNode();
            if (attrs.contains("label")) o.put("label", r.label());
            if (attrs.contains("value")) o.put("value", r.value());
            if (attrs.contains("color")) o.put("color", r.color());
            out.add(o);
        }
        return out;
    }

    // ---------- existing non bar/pie logic (unchanged) ----------
    private String detectShape(String id, JsonNode root) {
        if ("text".equalsIgnoreCase(id)) return "text";
        if ("line-chart".equalsIgnoreCase(id)) return "line-chart";
        if ("map-2d".equalsIgnoreCase(id)) return "map-2d";
        if ("network-2d".equalsIgnoreCase(id)) return "network-2d";
        if ("bar-chart".equalsIgnoreCase(id)) return "bar-chart";
        if ("pie-chart".equalsIgnoreCase(id)) return "pie-chart";

        if (root.isArray() && allObjectsHaveKeys(root, "label", "value")) return "bar-chart";
        if (root.isArray() && allObjectsHaveKeys(root, "name", "coordinates")) return "line-chart";
        if (root.isArray() && allObjectsHaveKeys(root, "type", "coordinates")) return "map-2d";
        if (root.isObject() && root.has("nodes") && root.has("links")) return "network-2d";
        if (root.isObject() && root.has("text") && root.has("datasets")) return "text";
        return "bar-chart";
    }

    private boolean allObjectsHaveKeys(JsonNode arr, String... keys) {
        for (JsonNode n : arr) {
            if (!n.isObject()) return false;
            for (String k : keys) if (!n.has(k)) return false;
        }
        return true;
    }

    private JsonNode filterLineChart(JsonNode root, Map<String, String> filters) {
        ArrayNode src = root.isArray() ? (ArrayNode) root : mapper.createArrayNode();
        Set<String> keepNames = parseCsvSet(filters.get("name"));
        Integer limit = parseInt(filters.get("limit"));

        List<ObjectNode> series = new ArrayList<>();
        for (JsonNode n : src) if (n.isObject()) series.add((ObjectNode) n);

        series.removeIf(n -> !keepNames.isEmpty() && !keepNames.contains(n.path("name").asText("")));

        if (limit != null && limit >= 0 && limit < series.size()) {
            series = series.subList(0, limit);
        }

        ArrayNode out = mapper.createArrayNode();
        for (ObjectNode s : series) out.add(s);
        return out;
    }

    private JsonNode filterMap2D(JsonNode root, Map<String, String> filters) {
        ArrayNode src = root.isArray() ? (ArrayNode) root : mapper.createArrayNode();
        Set<String> keepTypes = parseCsvSet(filters.get("type"));
        Set<String> keepLabels = parseCsvSet(filters.get("label"));
        Integer limit = parseInt(filters.get("limit"));

        List<ObjectNode> feats = new ArrayList<>();
        for (JsonNode n : src) if (n.isObject()) feats.add((ObjectNode) n);

        feats.removeIf(n -> {
            boolean typeOk = keepTypes.isEmpty() || keepTypes.contains(n.path("type").asText(""));
            boolean labelOk = keepLabels.isEmpty() || keepLabels.contains(n.path("label").asText(""));
            return !(typeOk && labelOk);
        });

        if (limit != null && limit >= 0 && limit < feats.size()) {
            feats = feats.subList(0, limit);
        }

        ArrayNode out = mapper.createArrayNode();
        for (ObjectNode f : feats) out.add(f);
        return out;
    }

    private JsonNode filterNetwork2D(JsonNode root, Map<String, String> filters) {
        if (!root.isObject()) return root;

        Set<Integer> keepNodes = parseCsvSet(filters.get("node")).stream()
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Integer limit = parseInt(filters.get("limit"));

        ArrayNode nodes = root.path("nodes").isArray() ? (ArrayNode) root.path("nodes") : mapper.createArrayNode();
        ArrayNode links = root.path("links").isArray() ? (ArrayNode) root.path("links") : mapper.createArrayNode();

        List<ObjectNode> keptNodes = new ArrayList<>();
        for (JsonNode n : nodes) {
            if (!n.isObject()) continue;
            int nid = n.path("id").asInt(Integer.MIN_VALUE);
            if (keepNodes.isEmpty() || keepNodes.contains(nid)) keptNodes.add((ObjectNode) n);
        }
        if (limit != null && limit >= 0 && limit < keptNodes.size()) {
            keptNodes = keptNodes.subList(0, limit);
        }
        Set<Integer> allowed = keptNodes.stream().map(n -> n.path("id").asInt()).collect(Collectors.toSet());

        List<ObjectNode> keptLinks = new ArrayList<>();
        for (JsonNode e : links) {
            if (!e.isObject()) continue;
            int s = e.path("source").asInt(Integer.MIN_VALUE);
            int t = e.path("target").asInt(Integer.MIN_VALUE);
            if (allowed.contains(s) && allowed.contains(t)) keptLinks.add((ObjectNode) e);
        }

        ObjectNode out = mapper.createObjectNode();
        ArrayNode outNodes = mapper.createArrayNode();
        keptNodes.forEach(outNodes::add);
        ArrayNode outLinks = mapper.createArrayNode();
        keptLinks.forEach(outLinks::add);
        out.set("nodes", outNodes);
        out.set("links", outLinks);
        return out;
    }

    // ---------- parsing utils ----------
    static final class Parsing {
        static Set<String> parseCsvSet(String csv) {
            if (csv == null || csv.isBlank()) return Collections.emptySet();
            return Arrays.stream(csv.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        static Integer parseInt(String s) {
            try {
                return (s == null || s.isBlank()) ? null : Integer.parseInt(s);
            } catch (Exception e) {
                return null;
            }
        }

        static Double parseDouble(String s) {
            try {
                return (s == null || s.isBlank()) ? null : Double.parseDouble(s);
            } catch (Exception e) {
                return null;
            }
        }
    }
}

