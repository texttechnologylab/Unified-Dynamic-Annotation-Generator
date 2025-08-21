package uni.textimager.sandbox.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataQueryHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final DummyDataProvider provider;

    public DataQueryHandler(DummyDataProvider provider) {
        this.provider = provider;
    }

    public String buildArrayJson(String id,
                                 String type,
                                 String attrsCsv,
                                 Map<String, String> filters,
                                 boolean pretty) {

        // Fetch a JSON payload for the id/type
        String raw = provider.getJsonFor(id, type);

        // Parse once; if parsing fails, return raw
        final JsonNode root;
        try {
            root = mapper.readTree(raw);
        } catch (Exception e) {
            return raw;
        }

        // Route by detected dataset shape
        String shape = detectShape(id, root);

        JsonNode result;
        switch (shape) {
            case "bar-chart":
            case "pie-chart":
                result = filterSortProjectBarPie(root, attrsCsv, filters);
                break;
            case "line-chart":
                result = filterLineChart(root, filters); // name, limit
                break;
            case "map-2d":
                result = filterMap2D(root, filters);     // type, label
                break;
            case "network-2d":
                result = filterNetwork2D(root, filters); // node
                break;
            case "text":
            default:
                result = root; // no filtering for text
        }

        try {
            return pretty
                    ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result)
                    : mapper.writeValueAsString(result);
        } catch (Exception e) {
            return raw;
        }
    }

    // ---------- shape detection ----------
    private String detectShape(String id, JsonNode root) {
        if ("text".equalsIgnoreCase(id)) return "text";
        if ("line-chart".equalsIgnoreCase(id)) return "line-chart";
        if ("map-2d".equalsIgnoreCase(id)) return "map-2d";
        if ("network-2d".equalsIgnoreCase(id)) return "network-2d";
        if ("bar-chart".equalsIgnoreCase(id)) return "bar-chart";
        if ("pie-chart".equalsIgnoreCase(id)) return "pie-chart";

        // Heuristics if id not explicit
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

    // ---------- bar/pie: full filter/sort/project ----------
    private JsonNode filterSortProjectBarPie(JsonNode root,
                                             String attrsCsv,
                                             Map<String, String> filters) {
        ArrayNode src = root.isArray() ? (ArrayNode) root : mapper.createArrayNode();

        // attrs default
        Set<String> attrs = Arrays.stream(Optional.ofNullable(attrsCsv).orElse("")
                        .split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (attrs.isEmpty()) attrs = new LinkedHashSet<>(List.of("label", "value", "color"));

        // filters
        Set<String> keepLabels = parseCsvSet(filters.get("label"));
        Double min = parseDouble(filters.get("min"));
        Double max = parseDouble(filters.get("max"));

        String sortKey = Optional.ofNullable(filters.get("sort")).orElse("value");
        boolean desc = Optional.ofNullable(filters.get("desc"))
                .map(v -> v.equalsIgnoreCase("true") || v.equals("1"))
                .orElse(true);
        Integer limit = parseInt(filters.get("limit"));

        // work list
        List<ObjectNode> rows = new ArrayList<>();
        for (JsonNode n : src) if (n.isObject()) rows.add((ObjectNode) n);

        // filter
        rows.removeIf(n -> {
            String lab = n.has("label") ? n.get("label").asText("") : "";
            double val = n.has("value") ? n.get("value").asDouble(0d) : 0d;
            if (!keepLabels.isEmpty() && !keepLabels.contains(lab)) return true;
            if (min != null && val < min) return true;
            if (max != null && val > max) return true;
            return false;
        });

        // sort
        if ("label".equalsIgnoreCase(sortKey)) {
            rows.sort(Comparator.comparing(n -> n.has("label") ? n.get("label").asText("") : ""));
        } else if ("value".equalsIgnoreCase(sortKey)) {
            rows.sort(Comparator.comparingDouble(n -> n.has("value") ? n.get("value").asDouble(0d) : 0d));
        }
        if (desc) Collections.reverse(rows);

        // limit
        if (limit != null && limit >= 0 && limit < rows.size()) {
            rows = rows.subList(0, limit);
        }

        // project
        ArrayNode out = mapper.createArrayNode();
        for (ObjectNode row : rows) {
            ObjectNode o = mapper.createObjectNode();
            for (String a : attrs) if (row.has(a)) o.set(a, row.get(a));
            out.add(o);
        }
        return out;
    }

    // ---------- line-chart: filter by dataset name; optional limit ----------
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

    // ---------- map-2d: filter by type and/or label; optional limit ----------
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

    // ---------- network-2d: keep subgraph induced by node ids ----------
    private JsonNode filterNetwork2D(JsonNode root, Map<String, String> filters) {
        if (!root.isObject()) return root;

        Set<Integer> keepNodes = parseCsvSet(filters.get("node")).stream()
                .map(s -> {
                    try { return Integer.parseInt(s); } catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Integer limit = parseInt(filters.get("limit")); // applies to nodes count

        ArrayNode nodes = root.path("nodes").isArray() ? (ArrayNode) root.path("nodes") : mapper.createArrayNode();
        ArrayNode links = root.path("links").isArray() ? (ArrayNode) root.path("links") : mapper.createArrayNode();

        // nodes filter
        List<ObjectNode> keptNodes = new ArrayList<>();
        for (JsonNode n : nodes) {
            if (!n.isObject()) continue;
            int id = n.path("id").asInt(Integer.MIN_VALUE);
            if (keepNodes.isEmpty() || keepNodes.contains(id)) keptNodes.add((ObjectNode) n);
        }
        if (limit != null && limit >= 0 && limit < keptNodes.size()) {
            keptNodes = keptNodes.subList(0, limit);
        }
        Set<Integer> allowed = keptNodes.stream().map(n -> n.path("id").asInt()).collect(Collectors.toSet());

        // links filter: both endpoints must remain
        List<ObjectNode> keptLinks = new ArrayList<>();
        for (JsonNode e : links) {
            if (!e.isObject()) continue;
            int s = e.path("source").asInt(Integer.MIN_VALUE);
            int t = e.path("target").asInt(Integer.MIN_VALUE);
            if (allowed.contains(s) && allowed.contains(t)) keptLinks.add((ObjectNode) e);
        }

        ObjectNode out = mapper.createObjectNode();
        ArrayNode outNodes = mapper.createArrayNode(); keptNodes.forEach(outNodes::add);
        ArrayNode outLinks = mapper.createArrayNode(); keptLinks.forEach(outLinks::add);
        out.set("nodes", outNodes);
        out.set("links", outLinks);
        return out;
    }

    // ---------- utils ----------
    private static Set<String> parseCsvSet(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Integer parseInt(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.parseInt(s); }
        catch (Exception e) { return null; }
    }

    private static Double parseDouble(String s) {
        try { return (s == null || s.isBlank()) ? null : Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }
}
