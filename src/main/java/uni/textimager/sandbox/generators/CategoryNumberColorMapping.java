package uni.textimager.sandbox.generators;

import lombok.Getter;
import lombok.NonNull;
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


public class CategoryNumberColorMapping extends CategoryNumberMapping implements CategoryNumberColorMappingInterface {
    @Getter
    HashMap<String, Color> categoryColorMap;

    public CategoryNumberColorMapping(String id, HashMap<String, HashMap<String, Double>> categoryNumberMap, HashMap<String, Color> categoryColorMap) {
        super(id, categoryNumberMap);
        this.categoryColorMap = categoryColorMap;
    }

    public CategoryNumberColorMapping(String id, HashMap<String, HashMap<String, Double>> categoryNumberMap) {
        super(id, categoryNumberMap);
        this.categoryColorMap = categoryColorMapFromCategoriesNumberMap(CategoryNumberMapping.calculateTotalFromCategoryCountMap(categoryNumberMap));
    }

    public CategoryNumberColorMapping(String id, CategoryNumberColorMapping copyOf) {
        super(id, copyOf);
        this.categoryColorMap = new HashMap<>();
        for (Map.Entry<String, Color> entry : copyOf.categoryColorMap.entrySet()) {
            this.categoryColorMap.put(entry.getKey(), new Color(entry.getValue().getRGB(), true));
        }
    }

    @Override
    public void multiplyByColor(@NonNull Color color) {

    }

    @Override
    public CategoryNumberColorMapping copy(String id) {
        return new CategoryNumberColorMapping(id, this);
    }

    @Override
    public void saveToDB(DBAccess dbAccess) throws SQLException {
        saveCategoryNumberMapToDB(dbAccess);
        saveCategoryColorMapToDB(dbAccess);
    }


    private void saveCategoryColorMapToDB(DBAccess dbAccess) throws SQLException {
        if (categoryColorMap == null || categoryColorMap.isEmpty()) return;

        try (Connection connection = dbAccess.getDataSource().getConnection()) {
            DSLContext dsl = DSL.using(connection);

            List<Query> inserts = new ArrayList<>();
            for (Map.Entry<String, Color> entry : categoryColorMap.entrySet()) {
                String category = entry.getKey();
                Color colorObj = entry.getValue();
                String color = String.format("#%02x%02x%02x", colorObj.getRed(), colorObj.getGreen(), colorObj.getBlue());

                inserts.add(dsl.insertInto(
                                DSL.table(DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR),
                                DSL.field(DBConstants.TABLEATTR_GENERATORID),
                                DSL.field(DBConstants.TABLEATTR_GENERATORDATA_CATEGORY),
                                DSL.field(DBConstants.TABLEATTR_GENERATORDATA_COLOR))
                        .values(id, category, color));
            }

            dsl.batch(inserts).execute();
        }
    }



    public static HashMap<String, Color> categoryColorMapFromCategoriesNumberMap(Map<String, Double> categoryNumberMap) {
        List<Color> distinctColors = Arrays.asList(
                Color.RED,
                Color.BLUE,
                Color.GREEN,
                Color.MAGENTA,
                Color.ORANGE,
                Color.CYAN,
                Color.YELLOW,
                Color.PINK,
                Color.GRAY,
                new Color(0, 128, 128),
                new Color(128, 0, 128),
                new Color(128, 128, 0),
                new Color(0, 0, 128),
                new Color(255, 105, 180),
                new Color(139, 69, 19),
                new Color(0, 255, 127),
                new Color(255, 165, 0),
                new Color(0, 191, 255),
                new Color(154, 205, 50)
        );

        HashMap<String, Color> categoryColorMap = new HashMap<>();
        Iterator<Color> colorIterator = distinctColors.iterator();

        List<String> sortedCategories = categoryNumberMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        for (String category : sortedCategories) {
            Color color;
            if (colorIterator.hasNext()) {
                color = colorIterator.next();
            } else {
                // Random colors if we run out of predefined colors
                color = new Color((int)(Math.random() * 0x1000000));
            }
            categoryColorMap.put(category, color);
        }

        return categoryColorMap;
    }
}
