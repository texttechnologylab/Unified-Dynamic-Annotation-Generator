package uni.textimager.sandbox.generators;

import uni.textimager.sandbox.sources.DBAccess;

import java.util.*;

public class SubstringCategoryMapping extends SubstringMapping implements SubstringCategoryMappingInterface {

    public SubstringCategoryMapping(String id, String text, List<CategorizedSubstring> categorizedSubstrings) {
        super(id, text, new ArrayList<>(categorizedSubstrings));
    }

    public SubstringCategoryMapping(String id, SubstringMapping copyOf) {
        super(id, copyOf);
    }

    @Override
    public GeneratorInterface copy(String id) {
        return new SubstringCategoryMapping(id, this);
    }

    @Override
    public void saveToDB(DBAccess dbAccess) {

    }

    public static class CategorizedSubstring extends Substring {

        public CategorizedSubstring(int begin, int end, String substringCategoryName, String substringCategoryValue) {
            super(begin, end, Map.of(substringCategoryName, substringCategoryValue), false, null, null, null, null, false, false, false, null, null);
        }

        public CategorizedSubstring(int begin, int end, String substringCategoryValue) {
            super(begin, end, Map.of("category", substringCategoryValue), false, null, null, null, null, false, false, false, null, null);
        }

        public CategorizedSubstring(Substring copyOf) {
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
