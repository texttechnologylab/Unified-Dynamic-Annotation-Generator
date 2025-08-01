package uni.textimager.sandbox.generators;

import lombok.Getter;

import java.util.*;

public class SubstringCategoryMapping extends SubstringMapping implements SubstringCategoryMappingInterface {

    public SubstringCategoryMapping(String text, List<CategorizedSubstring> categorizedSubstrings) {
        super(text, new ArrayList<>(categorizedSubstrings));
    }

    public SubstringCategoryMapping(SubstringMapping copyOf) {
        super(copyOf);
    }

    @Override
    public GeneratorInterface copy() {
        return new SubstringCategoryMapping(this);
    }

    public static class CategorizedSubstring extends FormattedSubstring {

        public CategorizedSubstring(int begin, int end, String substringCategoryName, String substringCategoryValue) {
            super(begin, end, Map.of(substringCategoryName, substringCategoryValue), false, null, null, null, null, false, false, false);
        }

        public CategorizedSubstring(int begin, int end, String substringCategoryValue) {
            super(begin, end, Map.of("category", substringCategoryValue), false, null, null, null, null, false, false, false);
        }

        public CategorizedSubstring(FormattedSubstring copyOf) {
            super(copyOf);
        }

        public String getCategory() {
            if (getCategoryValueMap().size() != 1) {
                throw new IllegalArgumentException("Multiple (or none) categories defined but CategorizedSubstring.getCategory() called (requires single category definition).");
            }
            return getCategoryValueMap().values().stream().findFirst().get();
        }
    }
}
