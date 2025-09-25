package uni.textimager.sandbox.generators;


import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.DSL;
import uni.textimager.sandbox.database.DBConstants;
import uni.textimager.sandbox.sources.DBAccess;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CategoryNumberMapping extends Generator implements CategoryNumberMappingInterface {
    Map<String, Map<String, Double>> categoryNumberMap;
    Color fixedColor;


    public CategoryNumberMapping(String id, Map<String, Map<String, Double>> categoryNumberMap) {
        super(id);
        this.categoryNumberMap = new HashMap<>();
        categoryNumberMap.forEach((k, v) -> this.categoryNumberMap.put(k, new HashMap<>(v)));
    }

    public CategoryNumberMapping(String id, Map<String, Map<String, Double>> categoryNumberMap, Color fixedColor) {
        this(id, categoryNumberMap);
        this.fixedColor = fixedColor;
    }

    public CategoryNumberMapping(String id, CategoryNumberMapping copyOf) {
        super(id);
        this.categoryNumberMap = new HashMap<>(copyOf.categoryNumberMap);
        this.fixedColor = copyOf.fixedColor;
    }

    @Override
    public void add(double num) {
        categoryNumberMap.replaceAll((file, innerMap) -> {
            innerMap.replaceAll((category, value) -> value + num);
            return innerMap;
        });
    }

    @Override
    public void subtract(double num) {
        categoryNumberMap.replaceAll((file, innerMap) -> {
            innerMap.replaceAll((category, value) -> value - num);
            return innerMap;
        });
    }

    @Override
    public void multiply(double num) {
        categoryNumberMap.replaceAll((file, innerMap) -> {
            innerMap.replaceAll((category, value) -> value * num);
            return innerMap;
        });
    }

    @Override
    public void divideBy(double num) {
        categoryNumberMap.replaceAll((file, innerMap) -> {
            innerMap.replaceAll((category, value) -> value / num);
            return innerMap;
        });
    }

    @Override
    public CategoryNumberMapping copy(String id) {
        return new CategoryNumberMapping(id, this);
    }

    @Override
    public void saveToDB(DBAccess dbAccess) throws SQLException {
        if (categoryNumberMap == null || categoryNumberMap.isEmpty()) return;
        if (fixedColor == null) {
            CategoryNumberColorMapping colorMapping = new CategoryNumberColorMapping(id, categoryNumberMap);
            colorMapping.saveToDB(dbAccess);
            return;
        }

        saveCategoryNumberMapToDB(dbAccess);
        String color = String.format("#%02x%02x%02x", fixedColor.getRed(), fixedColor.getGreen(), fixedColor.getBlue());
        try (Connection connection = dbAccess.getDataSource().getConnection()) {
            DSLContext dsl = DSL.using(connection);

            List<Query> inserts = new ArrayList<>();
            for (String category : CategoryNumberMapping.calculateTotalFromCategoryCountMap(categoryNumberMap).keySet()) {
                inserts.add(dsl.insertInto(DSL.table(DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR),
                                DSL.field(DBConstants.TABLEATTR_GENERATORID),
                                DSL.field(DBConstants.TABLEATTR_GENERATORDATA_CATEGORY),
                                DSL.field(DBConstants.TABLEATTR_GENERATORDATA_COLOR))
                        .values(id, category, color));
            }
            dsl.batch(inserts).execute();
        }
    }


    protected void saveCategoryNumberMapToDB(DBAccess dbAccess) throws SQLException {
        if (categoryNumberMap == null || categoryNumberMap.isEmpty()) return;

        try (Connection connection = dbAccess.getDataSource().getConnection()) {
            DSLContext dsl = DSL.using(connection);

            List<Query> inserts = new ArrayList<>();
            for (Map.Entry<String, Map<String, Double>> entry : categoryNumberMap.entrySet()) {
                String filename = entry.getKey();
                Map<String, Double> subMap = entry.getValue();

                for (Map.Entry<String, Double> subMapEntry : subMap.entrySet()) {
                    String category = subMapEntry.getKey();
                    Double value = subMapEntry.getValue();
                    inserts.add(dsl.insertInto(DSL.table(DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORID),
                                    DSL.field(DBConstants.TABLEATTR_FILENAME),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORDATA_CATEGORY),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORDATA_NUMBER))
                            .values(id, filename, category, value));

                }
            }
            dsl.batch(inserts).execute();
        }
    }

    public static Map<String, Map<String, Double>> capitalizeCategoryNumberKeys(Map<String, Map<String, Double>> categoryNumberMap, String capitalization) {
        HashMap<String, Map<String, Double>> resultMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : categoryNumberMap.entrySet()) {
            resultMap.put(entry.getKey(), capitalizeCategoryKeys(entry.getValue(), capitalization));
        }
        return resultMap;
    }

    public static <V> Map<String, V> capitalizeCategoryKeys(Map<String, V> categoryMap, String capitalization) {
        if (capitalization == null) { return new HashMap<>(categoryMap); }
        HashMap<String, V> resultMap = new HashMap<>();
        for (Map.Entry<String, V> entry : categoryMap.entrySet()) {
            if (capitalization.equalsIgnoreCase("Uppercase")) {
                resultMap.put(entry.getKey().toUpperCase(), entry.getValue());
            } else if (capitalization.equalsIgnoreCase("Lowercase")) {
                resultMap.put(entry.getKey().toLowerCase(), entry.getValue());
            } else if (capitalization.equalsIgnoreCase("Titlecase")) {
                resultMap.put(entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1).toLowerCase(), entry.getValue());
            } else {
                return new HashMap<>(categoryMap);
            }
        }
        return resultMap;
    }


    public static Map<String, Double> calculateTotalFromCategoryCountMap(Map<String, Map<String, Double>> categoryCountMap) {
        HashMap<String, Double> totals = new HashMap<>();

        for (Map<String, Double> innerMap : categoryCountMap.values()) {
            for (Map.Entry<String, Double> entry : innerMap.entrySet()) {
                String category = entry.getKey();
                Double count = entry.getValue();
                totals.merge(category, count, Double::sum);
            }
        }

        return totals;
    }

    public static Map<String, Map<String, Double>> keepTotalTopN(Map<String, Map<String, Double>> categoryCountMap, int n) {
        Set<String> topN = keepTopN(calculateTotalFromCategoryCountMap(categoryCountMap), n).keySet();
        HashMap<String, Map<String, Double>> resultMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : categoryCountMap.entrySet()) {
            resultMap.put(entry.getKey(), keepKeys(entry.getValue(), topN));
        }
        return resultMap;
    }

    public static Map<String, Double> keepTopN(Map<String, Double> categoryCountMap, int n) {
        if (n < 1) {
            return new HashMap<>(categoryCountMap);
        }
        if (categoryCountMap == null) {
            return new HashMap<>();
        }
        return categoryCountMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private static <K, V> Map<K, V> keepKeys(Map<K, V> original, Set<K> allowedKeys) {
        if (original == null || allowedKeys == null) {
            return Collections.emptyMap();
        }

        return original.entrySet().stream()
                .filter(entry -> allowedKeys.contains(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }
}
