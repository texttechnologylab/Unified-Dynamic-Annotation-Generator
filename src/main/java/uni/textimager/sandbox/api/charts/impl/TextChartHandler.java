package uni.textimager.sandbox.api.charts.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.api.ValueMode;
import uni.textimager.sandbox.api.Repositories.GeneratorDataRepository;
import uni.textimager.sandbox.api.charts.ChartHandler;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TextChartHandler implements ChartHandler {
    private final ObjectMapper mapper;
    private final GeneratorDataRepository repo;

    public TextChartHandler(ObjectMapper mapper, GeneratorDataRepository repo) {
        this.mapper = mapper;
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

    @Override
    public String getName() {
        return "HighlightText";
    }

    @Override
    public JsonNode render(String generatorId, Map<String, String> filters, Set<String> corpusFiles, ValueMode vm) {
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
                span.put("TEXT", text.substring(last, e.idx));
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
            span.put("TEXT", text.substring(last));
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

        root.put("generatorId", generatorId);
        root.put("textLength", text.length());
        root.set("spans", spans);
        root.set("datasets", mapper.createArrayNode());
        return root;
    }
}

