package uni.textimager.sandbox.generators;

import lombok.Getter;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class SubstringMapping extends Generator implements SubstringMappingInterface {
    private final String text;
    private final List<FormattedSubstring> formattedSubstrings;

    public SubstringMapping(String text, List<FormattedSubstring> formattedSubstrings) {
        this.text = text;
        this.formattedSubstrings = formattedSubstrings;
    }

    public SubstringMapping(SubstringMapping copyOf) {
        this.text = copyOf.text;
        this.formattedSubstrings = new ArrayList<>();
        for (FormattedSubstring fs : copyOf.formattedSubstrings) {
            this.formattedSubstrings.add(new FormattedSubstring(fs));
        }
    }

    @Getter
    public static class FormattedSubstring {
        private final int begin; // inclusive
        private final int end;  // exclusive
        private final Map<String, String> categoryValueMap;
        private final boolean preciseColorMode;
        private final Color color;
        private final Color colorText;
        private final Color colorHighlight;
        private final Color colorUnderlined;
        private final boolean underlined;
        private final boolean bold;
        private final boolean italic;

        public FormattedSubstring(int begin, int end, Map<String, String> categoryValueMap, boolean preciseColorMode, Color color, Color colorText, Color colorHighlight, Color colorUnderlined, boolean underlined, boolean bold, boolean italic) {
            if (begin < 0 || end <= begin) {
                throw new IllegalArgumentException("Invalid range: begin=" + begin + ", end=" + end);
            }
            this.begin = begin;
            this.end = end;
            this.categoryValueMap = categoryValueMap;
            this.preciseColorMode = preciseColorMode;
            this.color = color;
            this.colorText = colorText;
            this.colorHighlight = colorHighlight;
            this.colorUnderlined = colorUnderlined;
            this.underlined = underlined;
            this.bold = bold;
            this.italic = italic;
        }

        public FormattedSubstring(SubstringMapping.FormattedSubstring copyOf) {
            this.begin = copyOf.begin;
            this.end = copyOf.end;
            this.categoryValueMap = new HashMap<>(copyOf.categoryValueMap);
            this.preciseColorMode = copyOf.preciseColorMode;
            this.color = new Color(copyOf.color.getRGB(), true);
            this.colorText = new Color(copyOf.colorText.getRGB(), true);
            this.colorHighlight = new Color(copyOf.colorHighlight.getRGB(), true);
            this.colorUnderlined = new Color(copyOf.colorUnderlined.getRGB(), true);
            this.underlined = copyOf.underlined;
            this.bold = copyOf.bold;
            this.italic = copyOf.italic;
        }
    }
}
