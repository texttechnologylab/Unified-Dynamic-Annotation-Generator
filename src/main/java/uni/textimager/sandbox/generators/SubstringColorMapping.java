package uni.textimager.sandbox.generators;

import lombok.Getter;

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
    @Getter
    public static class ColoredSubstring {
        private final int begin; // inclusive
        private final int end;  // exclusive
        private final Color color;

        public ColoredSubstring(int begin, int end, Color color) {
            if (begin < 0 || end <= begin) {
                throw new IllegalArgumentException("Invalid range: begin=" + begin + ", end=" + end);
            }
            this.begin = begin;
            this.end = end;
            this.color = color;
        }

        public ColoredSubstring(ColoredSubstring copyOf) {
            this.begin = copyOf.begin;
            this.end = copyOf.end;
            this.color = new Color(copyOf.color.getRGB(), true);
        }

        @Override
        public String toString() {
            return "ColoredSubstring{begin=" + begin + ", end=" + end + ", color=" + color + '}';
        }
    }
}
