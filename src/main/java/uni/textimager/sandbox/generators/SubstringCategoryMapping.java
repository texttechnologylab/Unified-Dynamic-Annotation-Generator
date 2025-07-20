package uni.textimager.sandbox.generators;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

public class SubstringCategoryMapping extends Generator implements SubstringCategoryMappingInterface {
    private String text;
    private Collection<CategorizedSubstring> categorizedSubstrings;

    public SubstringCategoryMapping(String text, Collection<CategorizedSubstring> categorizedSubstrings) {
        this.text = text;
        this.categorizedSubstrings = categorizedSubstrings;
    }

    public SubstringCategoryMapping(SubstringCategoryMapping copyOf) {
        this.text = copyOf.text;
        this.categorizedSubstrings = new ArrayList<>();
        for (CategorizedSubstring cs : copyOf.categorizedSubstrings) {
            this.categorizedSubstrings.add(new CategorizedSubstring(cs));
        }
    }

    @Override
    public GeneratorInterface copy() {
        return new SubstringCategoryMapping(this);
    }


    // This class stands for a colored part of a text
    @Getter
    public static class CategorizedSubstring {
        private final int begin; // inclusive
        private final int end;  // exclusive
        private final String category;

        public CategorizedSubstring(int begin, int end, String category) {
            if (begin < 0 || end <= begin) {
                throw new IllegalArgumentException("Invalid range: begin=" + begin + ", end=" + end);
            }
            this.begin = begin;
            this.end = end;
            this.category = category;
        }

        public CategorizedSubstring(SubstringCategoryMapping.CategorizedSubstring copyOf) {
            this.begin = copyOf.begin;
            this.end = copyOf.end;
            this.category = copyOf.category;
        }

        @Override
        public String toString() {
            return "CategorizedSubstring{begin=" + begin + ", end=" + end + ", category=" + category + '}';
        }
    }
}
