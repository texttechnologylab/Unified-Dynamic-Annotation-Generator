package uni.textimager.sandbox.generators;


import lombok.NonNull;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import uni.textimager.sandbox.database.DBConstants;
import uni.textimager.sandbox.sources.DBAccess;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class CategoryNumberMapping extends Generator implements CategoryNumberMappingInterface{
    HashMap<String, Double> categoryNumberMap;

    boolean fractionModeEnabled;
    double fractionMode;
    double total;
    String numberSuffix;


    public CategoryNumberMapping(String id, HashMap<String, Double> categoryNumberMap) {
        super(id);
        this.categoryNumberMap = new HashMap<>(categoryNumberMap);
        fractionModeEnabled = false;
        numberSuffix = null;
        calculateTotal();
    }

    public CategoryNumberMapping(String id, CategoryNumberMapping copyOf) {
        super(id);
        this.categoryNumberMap = new HashMap<>(copyOf.categoryNumberMap);
        this.fractionModeEnabled = copyOf.fractionModeEnabled;
        this.fractionMode = copyOf.fractionMode;
        this.total = copyOf.total;
        this.numberSuffix = copyOf.numberSuffix;
    }

    @Override
    public void setFractionMode(double fractionMode) {
        fractionModeEnabled = true;
        this.fractionMode = fractionMode;
    }

    @Override
    public String getNumberString(@NonNull String category) {
        if (numberSuffix == null) {
            return Double.toString(getNumber(category));
        }
        return getNumber(category) + numberSuffix;
    }

    @Override
    public double getNumber(@NonNull String category) {
        Double num = categoryNumberMap.get(category);
        if (num == null) {
            throw new NoSuchElementException("Category not found: " + category);
        }
        if (fractionModeEnabled) {
            return num * (fractionMode / total);
        }
        return num;
    }

    @Override
    public double getTotal() {
        if (fractionModeEnabled) {
            return fractionMode;
        }
        return total;
    }

    @Override
    public void setNumberSuffix(String numberSuffix) {
        this.numberSuffix = numberSuffix;
    }

    @Override
    public void add(double num) {
        categoryNumberMap.replaceAll((key, value) -> value + num);
        calculateTotal();
    }

    @Override
    public void subtract(double num) {
        categoryNumberMap.replaceAll((key, value) -> value - num);
        calculateTotal();
    }

    @Override
    public void multiply(double num) {
        categoryNumberMap.replaceAll((key, value) -> value * num);
        calculateTotal();
    }

    @Override
    public void divideBy(double num) {
        categoryNumberMap.replaceAll((key, value) -> value / num);
        calculateTotal();
    }

    @Override
    public void round(int digits) {

    }

    @Override
    public CategoryNumberMapping copy(String id) {
        return new CategoryNumberMapping(id, this);
    }

    @Override
    public void saveToDB(DBAccess dbAccess) throws SQLException {
        saveToDB(dbAccess, null);
    }

    public void saveToDB(DBAccess dbAccess, Color fixedColor) throws SQLException {
        if (fixedColor == null) {
            CategoryNumberColorMapping colorMapping = new CategoryNumberColorMapping(id, categoryNumberMap);
            colorMapping.saveToDB(dbAccess);
            return;
        }

        String color = String.format("#%02x%02x%02x", fixedColor.getRed(), fixedColor.getGreen(), fixedColor.getBlue());
        try (Connection connection = dbAccess.getDataSource().getConnection()) {
            DSLContext dsl = DSL.using(connection);

            for (Map.Entry<String, Double> entry : categoryNumberMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .toList()) {
                String category = entry.getKey();
                Double value = entry.getValue();
                dsl.insertInto(DSL.table(DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBERCOLOR),
                                DSL.field(DBConstants.TABLEATTR_GENERATORID),
                                DSL.field(DBConstants.TABLEATTR_GENERATORDATA_CATEGORYNUMBERCOLOR_CATEGORY),
                                DSL.field(DBConstants.TABLEATTR_GENERATORDATA_CATEGORYNUMBERCOLOR_NUMBER),
                                DSL.field(DBConstants.TABLEATTR_GENERATORDATA_CATEGORYNUMBERCOLOR_COLOR))
                        .values(id, category, value, color)
                        .execute();

            }
        }
    }

    @Override
    public String generateJSONCategoricalChart(Color fixedColor) {
        if (fixedColor == null) {
            CategoryNumberColorMapping colorMapping = new CategoryNumberColorMapping(id, categoryNumberMap);
            return colorMapping.generateJSONCategoricalChart();
        }

        String color = String.format("#%02x%02x%02x", fixedColor.getRed(), fixedColor.getGreen(), fixedColor.getBlue());
        StringBuilder jsonStr = new StringBuilder("[\n");
        for (Map.Entry<String, Double> entry : categoryNumberMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList()) {
            String category = entry.getKey();
            Double value = entry.getValue();
            jsonStr.append("  {\"label\": \"").append(category).append("\", \"value\": ").append(value).append(", \"color\": \"").append(color).append("\"},\n");
        }
        jsonStr.setLength(jsonStr.length() - 2);
        jsonStr.append("\n]");
        return jsonStr.toString();
    }

    @Override
    public String generateJSONCategoricalChart() {
        return generateJSONCategoricalChart(null);
    }

    private void calculateTotal() {
        total = categoryNumberMap.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
