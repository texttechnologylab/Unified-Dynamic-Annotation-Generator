package uni.textimager.sandbox.importer.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class NameSanitizer {
    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while"
    ));
    private static final Set<String> SQL_RESERVED = Set.of(
            "begin", "end", "null", "select", "from", "table", "order", "group", "by", "user", "timestamp", "value"
    );

    public String sanitize(String name) {
        String s = name.replaceAll("[^A-Za-z0-9_]", "_");
        if (Character.isDigit(s.charAt(0))) s = "_" + s;
        if (JAVA_KEYWORDS.contains(s) || SQL_RESERVED.contains(s.toLowerCase())) s = "_" + s;
        return s;
    }

    public String toClassName(String tag) {
        String[] parts = tag.split(":");
        String base = parts[parts.length - 1];
        String cname = base.substring(0, 1).toUpperCase() + base.substring(1);
        if (SQL_RESERVED.contains(cname.toLowerCase())) cname = "_" + cname;
        return cname;
    }
}
