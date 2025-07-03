package uni.textimager.sandbox.importer.service;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import uni.textimager.sandbox.importer.EntityRecord;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DataInserterService {
    private static final Pattern IDENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private final NamedParameterJdbcTemplate jdbc;

    public DataInserterService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private void validate(String id) {
        if (!IDENT.matcher(id).matches()) throw new IllegalArgumentException(id);
    }

    public void insertRecords(List<EntityRecord> records) {
        var grouped = records.stream().collect(Collectors.groupingBy(EntityRecord::tag));
        for (var entry : grouped.entrySet()) {
            String table = entry.getKey();
            List<EntityRecord> list = entry.getValue();
            if (list.isEmpty()) continue;
            validate(table);
            List<String> cols = list.stream()
                    .flatMap(r -> r.attributes().keySet().stream())
                    .distinct()
                    .toList();
            cols.forEach(this::validate);

            String colCsv = String.join(",", cols);
            String paramCsv = cols.stream().map(c -> ":" + c).collect(Collectors.joining(","));
            String sql = "INSERT INTO " + table + "(" + colCsv + ") VALUES(" + paramCsv + ")";

            var params = list.stream().map(rec -> {
                var src = new MapSqlParameterSource();
                cols.forEach(col -> src.addValue(col, rec.attributes().get(col)));
                return src;
            }).toArray(MapSqlParameterSource[]::new);

            jdbc.batchUpdate(sql, params);
        }
    }
}

