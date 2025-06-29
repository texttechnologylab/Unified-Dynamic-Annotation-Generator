package uni.textimager.sandbox.generators;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CategoryNumberColorMapping extends CategoryNumberMapping implements CategoryNumberColorMappingInterface {
    private HashMap<String, Color> categoryColorMap;

    public CategoryNumberColorMapping(HashMap<String, Double> categoryNumberMap, HashMap<String, Color> categoryColorMap) {
        super(categoryNumberMap);
        this.categoryColorMap = categoryColorMap;
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
}
