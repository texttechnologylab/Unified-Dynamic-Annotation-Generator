package uni.textimager.sandbox.generators;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class SubstringColorMapping extends Generator implements SubstringColorMappingInterface {
    private String text;
    private Collection<ColoredSubstring> coloredSubstrings;


    public SubstringColorMapping(String text, Collection<ColoredSubstring> coloredSubstrings) {
        this.text = text;
        this.coloredSubstrings = coloredSubstrings;
    }

    public SubstringColorMapping(SubstringColorMapping copyOf) {
        this.text = copyOf.text;
        this.coloredSubstrings = new ArrayList<>();
        for (ColoredSubstring cs : copyOf.coloredSubstrings) {
            this.coloredSubstrings.add(new ColoredSubstring(cs));
        }
    }

    @Override
    public void multiplyByColor(Color color) {

    }

    @Override
    public SubstringColorMapping copy() {
        return new SubstringColorMapping(this);
    }



    // This class stands for a colored part of a text
    public static class ColoredSubstring {
        private final int start; // inclusive
        private final int end;  // exclusive
        private final Color color;

        public ColoredSubstring(int start, int end, Color color) {
            if (start < 0 || end <= start) {
                throw new IllegalArgumentException("Invalid range: start=" + start + ", end=" + end);
            }
            this.start = start;
            this.end = end;
            this.color = color;
        }

        public ColoredSubstring(ColoredSubstring copyOf) {
            this.start = copyOf.start;
            this.end = copyOf.end;
            this.color = new Color(copyOf.color.getRGB(), true);
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public Color getColor() {
            return color;
        }

        @Override
        public String toString() {
            return "ColoredSubstring{start=" + start + ", end=" + end + ", color=" + color + '}';
        }
    }
}
