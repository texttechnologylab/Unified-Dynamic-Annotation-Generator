package uni.textimager.sandbox.api.charts;

import uni.textimager.sandbox.api.Handler.ValueMode;
import uni.textimager.sandbox.api.Repositories.GeneratorDataRepository;

import java.util.*;

public final class ValueTransforms {
    private ValueTransforms() {
    }

    public static List<GeneratorDataRepository.BarPieRow> apply(
            List<GeneratorDataRepository.BarPieRow> rows,
            ValueMode mode,
            GeneratorDataRepository repo,
            String generatorId,
            Set<String> keepLabels,
            Set<String> corpusFiles
    ) {
        return switch (mode) {
            case RAW -> rows;
            case SHARE -> {
                double total = rows.stream().mapToDouble(GeneratorDataRepository.BarPieRow::value).sum();
                double denom = total == 0 ? 1 : total;
                yield rows.stream().map(r -> new GeneratorDataRepository.BarPieRow(r.label(), r.value() / denom, r.color())).toList();
            }
            case MAX1 -> {
                double max = rows.stream().mapToDouble(GeneratorDataRepository.BarPieRow::value).max().orElse(0);
                double denom = max == 0 ? 1 : max;
                yield rows.stream().map(r -> new GeneratorDataRepository.BarPieRow(r.label(), r.value() / denom, r.color())).toList();
            }
            case ZSCORE -> {
                double mean = rows.stream().mapToDouble(GeneratorDataRepository.BarPieRow::value).average().orElse(0);
                double var = rows.stream().mapToDouble(r -> {
                    double d = r.value() - mean;
                    return d * d;
                }).average().orElse(0);
                double sd = Math.sqrt(var);
                double denom = sd == 0 ? 1 : sd;
                yield rows.stream().map(r -> new GeneratorDataRepository.BarPieRow(r.label(), (r.value() - mean) / denom, r.color())).toList();
            }
            case PER_FILE_AVG -> {
                var perFile = repo.loadBarPiePerFile(generatorId, keepLabels, corpusFiles);
                Map<String, DoubleSummaryStatistics> byCat = new LinkedHashMap<>();
                Map<String, String> color = new LinkedHashMap<>();
                perFile.forEach(p -> {
                    byCat.computeIfAbsent(p.label(), k -> new DoubleSummaryStatistics()).accept(p.value());
                    color.putIfAbsent(p.label(), p.color());
                });
                yield byCat.entrySet().stream()
                        .map(e -> new GeneratorDataRepository.BarPieRow(e.getKey(), e.getValue().getAverage(),
                                color.getOrDefault(e.getKey(), "#999999")))
                        .toList();
            }
        };
    }

    public static List<GeneratorDataRepository.BarPieRow> sortLimitFilter(
            List<GeneratorDataRepository.BarPieRow> rows, String sortKey, boolean desc,
            Double min, Double max, Integer limit) {

        double minV = (min == null) ? Double.NEGATIVE_INFINITY : min;
        double maxV = (max == null) ? Double.POSITIVE_INFINITY : max;

        var filtered = rows.stream()
                .filter(r -> r.value() >= minV && r.value() <= maxV);

        Comparator<GeneratorDataRepository.BarPieRow> cmp =
                "label".equalsIgnoreCase(sortKey)
                        ? Comparator.comparing(GeneratorDataRepository.BarPieRow::label, String.CASE_INSENSITIVE_ORDER)
                        : Comparator.comparingDouble(GeneratorDataRepository.BarPieRow::value);
        if (desc) cmp = cmp.reversed();

        var sorted = filtered.sorted(cmp).toList();

        if (limit != null && limit >= 0 && limit < sorted.size()) return sorted.subList(0, limit);
        return sorted;
    }
}
