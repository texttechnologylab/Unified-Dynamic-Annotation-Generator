package uni.textimager.sandbox.database;

public class NameMapper {
    public static String mapField(String table, String field) {
        String normalized = field.toUpperCase().replace(":", "_");
        return switch (normalized) {
            case "BEGIN" -> table.toUpperCase() + "_BEGIN";
            case "END" -> table.toUpperCase() + "_END";
            default -> table.toUpperCase() + "_" + normalized;
        };
    }

    public static String mapTable(String name) {
        return name.toUpperCase();
    }
}

