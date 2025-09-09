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

@Service
public class DataQueryHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final DummyDataProvider provider; // kept for non DB-backed shapes
    private final GeneratorDataRepository repo;

    public DataQueryHandler(DummyDataProvider provider, GeneratorDataRepository repo) {
        this.provider = provider;
        this.repo = repo;
    }

    private static Set<String> csvSet(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        Set<String> s = new LinkedHashSet<>();
        for (String p : csv.split(",")) {
            String v = p.trim();
            if (!v.isEmpty()) s.add(v);
        }
        return s;
    }

    private static String toCss(String styleName, String color) {
        return switch (styleName == null ? "" : styleName.toLowerCase(Locale.ROOT)) {
            case "bold" -> "color: " + color + "; font-weight: bold;";
            case "underline" -> "text-decoration: underline 2px " + color + ";";
            case "highlight" -> "background-color: " + color + ";";
            default -> "";
        };
    }

    public String buildArrayJson(String id,
                                 String type,
                                 String attrsCsv,
                                 Map<String, String> filters,
                                 boolean pretty) {

        String shape = normalizedShape(type, id);

        // TEXT → return spans JSON string immediately
        if ("text".equals(shape)) {
            return buildTextSpansJson(id, filters, pretty); // id == generatorId
        }

        // DB bar/pie → build JsonNode then serialize
        JsonNode result;
        if ("bar-chart".equals(shape) || "pie-chart".equals(shape)) {
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
            if (t.contains("highlight") && t.contains("text")) return "text";
            switch (t) {
                case "text" -> {
                    return "text";
                }
                case "barchart", "bar-chart" -> {
                    return "bar-chart";
                }
                case "piechart", "pie-chart" -> {
                    return "pie-chart";
                }
            }
            if (Set.of("line-chart", "map-2d", "network-2d").contains(t)) return t;
        }
        if (id != null) {
            String i = id.trim().toLowerCase(Locale.ROOT);
            if (Set.of("bar-chart", "pie-chart", "line-chart", "map-2d", "network-2d", "text").contains(i)) return i;
        }
        return "bar-chart";
    }

    // ---------- DB bar/pie ----------
    private JsonNode dbBarPie(String generatorId,
                              String attrsCsv,
                              Map<String, String> filters) {

        LinkedHashSet<String> attrs = Arrays.stream(Optional.ofNullable(attrsCsv).orElse("")
                        .split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (attrs.isEmpty()) attrs = new LinkedHashSet<>(List.of("label", "value", "color"));

        Set<String> keepLabels = Parsing.parseCsvSet(filters.get("label"));
        Double min = Parsing.parseDouble(filters.get("min"));
        Double max = Parsing.parseDouble(filters.get("max"));
        String sortKey = Optional.ofNullable(filters.get("sort")).orElse("value");
        boolean desc = Optional.ofNullable(filters.get("desc"))
                .map(v -> v.equalsIgnoreCase("true") || v.equals("1"))
                .orElse(true);
        Integer limit = Parsing.parseInt(filters.get("limit"));

        List<GeneratorDataRepository.BarPieRow> rows =
                repo.loadBarPie(generatorId, keepLabels, min, max, sortKey, desc, limit);

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

    // ---------- TEXT (spans JSON) ----------
    private String buildTextSpansJson(String generatorId, Map<String, String> filters, boolean pretty) {
        // Toggles: include lists
        boolean typesProvided = filters.containsKey("types");
        boolean categoriesProvided = filters.containsKey("categories");
        boolean stylesProvided = filters.containsKey("styles"); // optional

        Set<String> includeTypes = csvSet(filters.get("types"));
        Set<String> includeCategories = csvSet(filters.get("categories"));
        Set<String> includeStyles = csvSet(filters.get("styles")).stream()
                .map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toCollection(LinkedHashSet::new));

        String text = repo.getText(generatorId);
        Map<String, String> typeStyles = repo.getTypeStyles(generatorId); // type -> styleName
        Map<String, Map<String, String>> colors = repo.getTypeCategoryColors(generatorId); // type -> (category -> color)

        // DB include filter for type/category where possible
        Set<String> typeDbFilter = typesProvided && includeTypes.isEmpty() ? Set.of("__NONE__") : (typesProvided ? includeTypes : null);
        Set<String> catDbFilter = categoriesProvided && includeCategories.isEmpty() ? Set.of("__NONE__") : (categoriesProvided ? includeCategories : null);

        List<GeneratorDataRepository.Segment> segs = (typeDbFilter == null && catDbFilter == null)
                ? repo.getSegments(generatorId, null, null)
                : repo.getSegments(generatorId,
                (typeDbFilter == null || typeDbFilter.contains("__NONE__")) ? Set.of() : typeDbFilter,
                (catDbFilter == null || catDbFilter.contains("__NONE__")) ? Set.of() : catDbFilter);

        // If key existed but list is empty → no segments
        if ((typesProvided && includeTypes.isEmpty()) || (categoriesProvided && includeCategories.isEmpty())) {
            segs = Collections.emptyList();
        }

        // Optional style filter
        if (stylesProvided && !includeStyles.isEmpty()) {
            segs = segs.stream()
                    .filter(s -> includeStyles.contains(
                            Optional.ofNullable(typeStyles.get(s.type()))
                                    .orElse("").toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        int N = text.length();

        record Ev(int idx, boolean start, String styleCss, String labelHtml) {
        }
        List<Ev> evs = new ArrayList<>(segs.size() * 2);

        for (var s : segs) {
            String styleName = typeStyles.getOrDefault(s.type(), "");
            String color = Optional.ofNullable(colors.get(s.type()))
                    .map(m -> m.get(s.category()))
                    .orElse("#000000");

            String css = toCss(styleName, color);
            String labelHtml = "<span style=\"color: " + color + ";\">" + s.category() + "</span>";

            int b = Math.max(0, Math.min(N, s.begin()));
            int e = Math.max(0, Math.min(N, s.end()));
            if (e < b) {
                int tmp = b;
                b = e;
                e = tmp;
            }

            evs.add(new Ev(b, true, css, labelHtml));
            evs.add(new Ev(e, false, css, labelHtml));
        }

        evs.sort(Comparator.<Ev>comparingInt(e -> e.idx).thenComparing(e -> e.start ? 1 : 0)); // end before start

        List<String> activeCss = new ArrayList<>();
        List<String> activeLbl = new ArrayList<>();
        int last = 0;
        ArrayNode spans = mapper.createArrayNode();

        for (Ev e : evs) {
            if (last < e.idx) {
                ObjectNode span = mapper.createObjectNode();
                span.put("text", text.substring(last, e.idx));
                if (!activeCss.isEmpty()) span.put("style", String.join(" ", activeCss));
                if (!activeLbl.isEmpty()) span.put("label", String.join(" ", activeLbl));
                spans.add(span);
            }
            if (e.start) {
                if (!activeCss.contains(e.styleCss)) activeCss.add(e.styleCss);
                if (!activeLbl.contains(e.labelHtml)) activeLbl.add(e.labelHtml);
            } else {
                activeCss.remove(e.styleCss);
                activeLbl.remove(e.labelHtml);
            }
            last = e.idx;
        }

        if (last < N) {
            ObjectNode span = mapper.createObjectNode();
            span.put("text", text.substring(last));
            spans.add(span);
        }

        ArrayNode datasets = mapper.createArrayNode();
        typeStyles.keySet().forEach(t -> {
            ObjectNode d = mapper.createObjectNode();
            d.put("name", t);
            datasets.add(d);
        });

        ObjectNode root = mapper.createObjectNode();
        root.put("generatorId", generatorId);
        root.put("textLength", N);
        root.set("spans", spans);
        root.set("datasets", datasets);

        try {
            return pretty
                    ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
                    : mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- existing non DB helpers ----------
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
        Set<String> keepNames = Parsing.parseCsvSet(filters.get("name"));
        Integer limit = Parsing.parseInt(filters.get("limit"));

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
        Set<String> keepTypes = Parsing.parseCsvSet(filters.get("type"));
        Set<String> keepLabels = Parsing.parseCsvSet(filters.get("label"));
        Integer limit = Parsing.parseInt(filters.get("limit"));

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

        Set<Integer> keepNodes = Parsing.parseCsvSet(filters.get("node")).stream()
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Integer limit = Parsing.parseInt(filters.get("limit"));

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

    // -------- parsing utils used by legacy filters --------
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
