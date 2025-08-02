package uni.textimager.sandbox.generators;

import lombok.Getter;

import java.awt.*;
import java.util.*;
import java.util.List;


public class CategoryNumberColorMapping extends CategoryNumberMapping implements CategoryNumberColorMappingInterface {
    @Getter
    HashMap<String, Color> categoryColorMap;

    public CategoryNumberColorMapping(HashMap<String, Double> categoryNumberMap, HashMap<String, Color> categoryColorMap) {
        super(categoryNumberMap);
        this.categoryColorMap = categoryColorMap;
    }

    public CategoryNumberColorMapping(HashMap<String, Double> categoryNumberMap) {
        super(categoryNumberMap);
        this.categoryColorMap = categoryColorMapFromCategoriesNumberMap(categoryNumberMap);
    }

    public CategoryNumberColorMapping(CategoryNumberColorMapping copyOf) {
        super(copyOf);
        this.categoryColorMap = new HashMap<>();
        for (Map.Entry<String, Color> entry : copyOf.categoryColorMap.entrySet()) {
            this.categoryColorMap.put(entry.getKey(), new Color(entry.getValue().getRGB(), true));
        }
    }

    @Override
    public void multiplyByColor(Color color) {

    }

    @Override
    public CategoryNumberColorMapping copy() {
        return new CategoryNumberColorMapping(this);
    }

    @Override
    public String generateJSONCategoricalChart() {
        StringBuilder jsonStr = new StringBuilder("[\n");
        for (Map.Entry<String, Double> entry : categoryNumberMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList()) {
            String category = entry.getKey();
            Double value = entry.getValue();
            String color = String.format("#%02x%02x%02x", categoryColorMap.get(category).getRed(), categoryColorMap.get(category).getGreen(), categoryColorMap.get(category).getBlue());
            jsonStr.append("  {\"label\": \"").append(category).append("\", \"value\": ").append(value).append(", \"color\": \"").append(color).append("\"},\n");
        }
        jsonStr.setLength(jsonStr.length() - 2);
        jsonStr.append("\n]");
        return jsonStr.toString();
    }


    private static HashMap<String, Color> categoryColorMapFromCategoriesNumberMap(Map<String, Double> categoryNumberMap) {
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
