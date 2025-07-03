package uni.textimager.sandbox.importer.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class DynamicTableService {
    private static final Pattern IDENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final JdbcTemplate jdbc;

    public DynamicTableService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private void validateIdentifier(String id) {
        if (!IDENT.matcher(id).matches())
            throw new IllegalArgumentException("Invalid identifier: " + id);
    }

    public List<String> listTables() {
        return jdbc.queryForList(
                "SELECT table_name FROM TableNames",
                String.class
        );
    }

    public List<String> listColumns(String table) {
        validateIdentifier(table);
        return jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = ?",
                String.class,
                table
        );
    }

    public List<Map<String,Object>> selectAll(String table) {
        validateIdentifier(table);
        return jdbc.queryForList(
                "SELECT * FROM " + table
        );
    }

    public List<Map<String,Object>> selectWhere(
            String table,
            String whereClause,
            Object... params
    ) {
        validateIdentifier(table);
        return jdbc.queryForList(
                "SELECT * FROM " + table + " WHERE " + whereClause,
                params
        );
    }

    public int updateWhere(
            String table,
            Map<String,Object> values,
            String whereClause,
            Object... params
    ) {
        validateIdentifier(table);
        var setClause = String.join(
                ", ",
                values.keySet().stream()
                        .peek(this::validateIdentifier)
                        .map(k -> k + " = ?")
                        .toList()
        );
        var allParams = values.values().stream().toList();
        return jdbc.update(
                "UPDATE " + table + " SET " + setClause + " WHERE " + whereClause,
                concat(allParams, List.of(params)).toArray()
        );
    }

    public int deleteWhere(
            String table,
            String whereClause,
            Object... params
    ) {
        validateIdentifier(table);
        return jdbc.update(
                "DELETE FROM " + table + " WHERE " + whereClause,
                params
        );
    }

    private List<Object> concat(List<Object> first, List<Object> second) {
        var result = new java.util.ArrayList<>(first);
        result.addAll(second);
        return result;
    }
}
