package uni.textimager.sandbox.generators;

import uni.textimager.sandbox.sources.DBAccess;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SubstringColorMapping extends SubstringMapping implements SubstringColorMappingInterface {

    public SubstringColorMapping(String id, String text, List<ColoredSubstring> coloredSubstrings) {
        super(id, text, new ArrayList<>(coloredSubstrings));
    }

    public SubstringColorMapping(String id, SubstringMapping copyOf) {
        super(id, copyOf);
    }

    @Override
    public void multiplyByColor(Color color) {

    }

    @Override
    public SubstringColorMapping copy(String id) {
        return new SubstringColorMapping(id, this);
    }

    @Override
    public void saveToDB(DBAccess dbAccess) {

    }

    public static class ColoredSubstring extends Substring {

        public ColoredSubstring(int begin, int end, Color color) {
            super(begin, end, Map.of(), false, color, null, null, null, false, false, false, null, null);
        }

        public ColoredSubstring(int begin, int end, Color color, Map<String, String> categoryValueMap) {
            super(begin, end, new HashMap<>(categoryValueMap), false, color, null, null, null, false, false, false, null, null);
        }

        public ColoredSubstring(Substring copyOf) {
            super(copyOf);
        }


    }
}
