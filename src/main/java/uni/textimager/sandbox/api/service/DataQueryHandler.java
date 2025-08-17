package uni.textimager.sandbox.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    /**
     * Returns a JSON array string. Supports:
     *  - attrs=name1,name2 to project fields (default: label,value,color)
     *  - label=NN,KON to keep only these labels
     *  - min=number / max=number to filter by value range
     *  - sort=label|value to sort
     *  - desc=true to reverse sort
     *  - limit=N to cap result length
     */
    public String buildArrayJson(String id, String type, String attrsCsv,
                                 Map<String, String> filters, boolean pretty) {
        Set<String> attrs = Arrays.stream(attrsCsv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (attrs.isEmpty()) attrs = new LinkedHashSet<>(List.of("label", "value", "color"));

        // source data (dummy seeded)
        List<Map<String, Object>> rows = new ArrayList<>(provider.getData(id, type));

        // filters
        Set<String> keepLabels = parseCsvSet(filters.get("label"));
        Double min = parseDouble(filters.get("min"));
        Double max = parseDouble(filters.get("max"));
        String sort = Optional.ofNullable(filters.get("sort")).orElse("");
        boolean desc = "true".equalsIgnoreCase(filters.get("desc")) || "1".equals(filters.get("desc"));
        Integer limit = parseInt(filters.get("limit"));

        rows.removeIf(m -> {
            String lab = Objects.toString(m.get("label"), "");
            double val = toDouble(m.get("value"));
            if (!keepLabels.isEmpty() && !keepLabels.contains(lab)) return true;
            if (min != null && val < min) return true;
            if (max != null && val > max) return true;
            return false;
        });

        if ("label".equalsIgnoreCase(sort)) {
            rows.sort(Comparator.comparing(m -> Objects.toString(m.get("label"), "")));
        } else if ("value".equalsIgnoreCase(sort)) {
            rows.sort(Comparator.comparingDouble(m -> toDouble(m.get("value"))));
        }
        if (desc) Collections.reverse(rows);
        if (limit != null && limit >= 0 && limit < rows.size()) {
            rows = rows.subList(0, limit);
        }

        ArrayNode arr = mapper.createArrayNode();
        for (Map<String, Object> m : rows) {
            ObjectNode o = mapper.createObjectNode();
            for (String a : attrs) {
                if (m.containsKey(a)) o.putPOJO(a, m.get(a));
            }
            arr.add(o);
        }

        try {
            return pretty ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arr)
                    : mapper.writeValueAsString(arr);
        } catch (Exception e) {
            return "[]";
        }
    }

    private static Set<String> parseCsvSet(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Double parseDouble(String s) {
        try { return (s == null || s.isBlank()) ? null : Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }

    private static Integer parseInt(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.parseInt(s); }
        catch (Exception e) { return null; }
    }

    private static double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(Objects.toString(o, "0")); }
        catch (Exception e) { return 0d; }
    }
}
