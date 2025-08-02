package uni.textimager.sandbox.generators;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SubstringColorMapping extends SubstringMapping implements SubstringColorMappingInterface {

    public SubstringColorMapping(String text, List<ColoredSubstring> coloredSubstrings) {
        super(text, new ArrayList<>(coloredSubstrings));
    }

    public SubstringColorMapping(SubstringMapping copyOf) {
        super(copyOf);
    }

    @Override
    public void multiplyByColor(Color color) {

    }

    @Override
    public SubstringColorMapping copy() {
        return new SubstringColorMapping(this);
    }

    public static class ColoredSubstring extends Substring {

        public ColoredSubstring(int begin, int end, Color color) {
            super(begin, end, Map.of(), false, color, null, null, null, false, false, false);
        }

        public ColoredSubstring(int begin, int end, Color color, Map<String, String> categoryValueMap) {
            super(begin, end, new HashMap<>(categoryValueMap), false, color, null, null, null, false, false, false);
        }

        public ColoredSubstring(Substring copyOf) {
            super(copyOf);
        }


    }
}
