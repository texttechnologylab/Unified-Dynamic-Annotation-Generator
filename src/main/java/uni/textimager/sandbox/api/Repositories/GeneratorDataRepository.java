package uni.textimager.sandbox.api.Repositories;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.stereotype.Repository;
import uni.textimager.sandbox.database.DBConstants;

import java.util.*;

import static org.jooq.impl.DSL.*;

@Repository
public class GeneratorDataRepository {

    private final DSLContext dsl;

    public GeneratorDataRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    // ---- helpers: schema-qualified names ----
    private Table<?> T_CATNUM(String schema) {
        return table(name(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER));
    }

    private Table<?> T_CATCOLOR(String schema) {
        return table(name(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR));
    }

    private Table<?> T_TYPECATCOLOR(String schema) {
        return table(name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR));
    }

    private Table<?> T_TEXT(String schema) {
        return table(name(schema, DBConstants.TABLENAME_GENERATORDATA_TEXT));
    }

    private Table<?> T_TYPESTYLE(String schema) {
        return table(name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE));
    }

    private Table<?> T_TYPESEG(String schema) {
        return table(name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS));
    }

    private Field<String> F_GEN_ID(String schema, String table) {
        return field(name(schema, table, DBConstants.TABLEATTR_GENERATORID), String.class);
    }

    private Field<String> F_FILENAME(String schema) {
        return field(name(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER, DBConstants.TABLEATTR_FILENAME), String.class);
    }

    private Field<String> F_CATEGORY(String schema, String table) {
        return field(name(schema, table, DBConstants.TABLEATTR_GENERATORDATA_CATEGORY), String.class);
    }

    private Field<Double> F_NUMBER(String schema) {
        return field(name(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER, DBConstants.TABLEATTR_GENERATORDATA_NUMBER), Double.class);
    }

    private Field<String> F_COLOR(String schema, String table) {
        return field(name(schema, table, DBConstants.TABLEATTR_GENERATORDATA_COLOR), String.class);
    }

    private Field<String> F_TYPE(String schema, String table) {
        return field(name(schema, table, DBConstants.TABLEATTR_GENERATORDATA_TYPE), String.class);
    }

    // ---------- CategoryNumber data ----------

    /**
     * Return category->sum(number) (optionally filtered by files), plus a category->color map (fallback to type-specific then plain).
     */
    public ResultCategoryNumber loadCategoryNumber(String schema,
                                                   String generatorId,
                                                   Set<String> files,
                                                   String typeForColors // may be null
    ) {
        // numeric values
        var T = T_CATNUM(schema);
        var GEN = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER);
        var FN = F_FILENAME(schema);
        var CAT = F_CATEGORY(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER);
        var NUM = F_NUMBER(schema);

        var step = dsl.select(CAT, sum(NUM).as("value"))
                .from(T)
                .where(GEN.eq(generatorId));

        if (files != null && !files.isEmpty()) {
            step = step.and(FN.in(files));
        }

        Map<String, Double> values = new LinkedHashMap<>();
        step.groupBy(CAT)
                .orderBy(field("value").desc())
                .fetch()
                .forEach(r -> values.put(r.get(CAT), r.get("value", Double.class)));

        // colors (type-specific first, then plain)
        Map<String, String> colors = new HashMap<>();
        if (typeForColors != null) {
            var TTC = T_TYPECATCOLOR(schema);
            var TC_GEN = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR);
            var TC_TYPE = F_TYPE(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR);
            var TC_CAT = F_CATEGORY(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR);
            var TC_COL = F_COLOR(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR);
            dsl.select(TC_CAT, TC_COL)
                    .from(TTC)
                    .where(TC_GEN.eq(generatorId).and(TC_TYPE.eq(typeForColors)))
                    .fetch()
                    .forEach(r -> colors.putIfAbsent(r.get(TC_CAT), r.get(TC_COL)));
        }

        var TCC = T_CATCOLOR(schema);
        var CC_GEN = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR);
        var CC_CAT = F_CATEGORY(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR);
        var CC_COL = F_COLOR(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR);
        dsl.select(CC_CAT, CC_COL)
                .from(TCC)
                .where(CC_GEN.eq(generatorId))
                .fetch()
                .forEach(r -> colors.putIfAbsent(r.get(CC_CAT), r.get(CC_COL)));

        return new ResultCategoryNumber(values, colors);
    }

    // ---------- Text & segments ----------
    public Optional<String> loadText(String schema, String generatorId) {
        var T = T_TEXT(schema);
        var GEN = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_TEXT);
        var F_TEXT = field(name(schema, DBConstants.TABLENAME_GENERATORDATA_TEXT,
                DBConstants.TABLEATTR_GENERATORDATA_TEXT), String.class);

        return dsl.select(F_TEXT)
                .from(T)
                .where(GEN.eq(generatorId))
                .fetchOptional(F_TEXT);
    }

    public List<SegmentRow> loadSegments(String schema, String generatorId, String typeOrNull) {
        var T = T_TYPESEG(schema);
        var GEN = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS);
        var TYPE = F_TYPE(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS);
        var BEGIN = field(name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS, DBConstants.TABLEATTR_GENERATORDATA_BEGIN), Integer.class);
        var END = field(name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS, DBConstants.TABLEATTR_GENERATORDATA_END), Integer.class);
        var CAT = F_CATEGORY(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS);

        var step = dsl.select(BEGIN, END, CAT, TYPE)
                .from(T)
                .where(GEN.eq(generatorId));

        if (typeOrNull != null) {
            step = step.and(TYPE.eq(typeOrNull));
        }

        return step.orderBy(BEGIN.asc(), END.asc())
                .fetch(r -> new SegmentRow(
                        r.get(BEGIN),
                        r.get(END),
                        r.get(CAT),
                        r.get(TYPE)
                ));
    }

    public Map<String, String> loadTypeStyles(String schema, String generatorId) {
        var T = T_TYPESTYLE(schema);
        var GEN = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE);
        var TYPE = F_TYPE(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE);
        var STYLE = field(name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE, DBConstants.TABLEATTR_GENERATORDATA_STYLE), String.class);

        Map<String, String> styles = new LinkedHashMap<>();
        dsl.select(TYPE, STYLE)
                .from(T)
                .where(GEN.eq(generatorId))
                .fetch()
                .forEach(r -> styles.put(r.get(TYPE), r.get(STYLE)));
        return styles;
    }

    public Map<String, Map<String, String>> loadTypeCategoryColors(String schema, String generatorId) {
        // Tables & fields
        var T_SPEC = T_TYPECATCOLOR(schema);
        var GEN_SPEC = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR);
        var TYPE = F_TYPE(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR);
        var CAT_SPEC = F_CATEGORY(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR);
        var COL_SPEC = F_COLOR(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR);

        var T_GEN = T_CATCOLOR(schema);
        var GEN_GEN = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR);
        var CAT_GEN = F_CATEGORY(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR);
        var COL_GEN = F_COLOR(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR);

        // 1) generic category -> color
        Map<String, String> generic = new LinkedHashMap<>();
        dsl.select(CAT_GEN, COL_GEN)
                .from(T_GEN)
                .where(GEN_GEN.eq(generatorId))
                .fetch()
                .forEach(r -> generic.put(r.get(CAT_GEN), r.get(COL_GEN)));

        // 2) type-specific: type -> (category -> color)
        Map<String, Map<String, String>> specific = new LinkedHashMap<>();
        dsl.select(TYPE, CAT_SPEC, COL_SPEC)
                .from(T_SPEC)
                .where(GEN_SPEC.eq(generatorId))
                .fetch()
                .forEach(r -> {
                    String t = r.get(TYPE);
                    String cat = r.get(CAT_SPEC);
                    String col = r.get(COL_SPEC);
                    specific.computeIfAbsent(t, k -> new LinkedHashMap<>()).put(cat, col);
                });

        // 3) Determine all types we care about (from segments + styles + specific colors)
        Set<String> types = new LinkedHashSet<>();

        // from TYPESEGMENTS (distinct type)
        {
            var T_SEG = T_TYPESEG(schema);
            var GEN_SEG = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS);
            var TYPE_SEG = F_TYPE(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS);
            dsl.selectDistinct(TYPE_SEG)
                    .from(T_SEG)
                    .where(GEN_SEG.eq(generatorId))
                    .fetch(TYPE_SEG)
                    .forEach(types::add);
        }

        // from TYPESTYLE
        {
            var T_TS = T_TYPESTYLE(schema);
            var GEN_TS = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE);
            var TYPE_TS = F_TYPE(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE);
            dsl.selectDistinct(TYPE_TS)
                    .from(T_TS)
                    .where(GEN_TS.eq(generatorId))
                    .fetch(TYPE_TS)
                    .forEach(types::add);
        }

        // from specific color table itself
        types.addAll(specific.keySet());

        // 4) Build final: for each type, start with generic colors then overlay specific
        Map<String, Map<String, String>> out = new LinkedHashMap<>();
        for (String t : types) {
            Map<String, String> m = new LinkedHashMap<>(generic);
            Map<String, String> spec = specific.get(t);
            if (spec != null) m.putAll(spec); // type-specific overrides generic
            out.put(t, m);
        }

        // Edge case: no types at all — return empty map (handler will default color)
        return out;
    }


    public Map<String, Map<String, Double>> loadCategoryNumberPerFile(
            String schema,
            String generatorId,
            String typeForColors // not used here, but keep signature consistent if you want
    ) {
        var T = T_CATNUM(schema);
        var GEN = F_GEN_ID(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER);
        var FN = F_FILENAME(schema);
        var CAT = F_CATEGORY(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER);
        var NUM = F_NUMBER(schema);

        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        dsl.select(FN, CAT, sum(NUM).as("value"))
                .from(T)
                .where(GEN.eq(generatorId))
                .groupBy(FN, CAT)
                .orderBy(FN.asc(), field("value").desc())
                .fetch(r -> {
                    result.computeIfAbsent(r.get(FN), k -> new LinkedHashMap<>())
                            .put(r.get(CAT), r.get("value", Double.class));
                    return null;
                });
        return result; // filename -> (category -> value)
    }

    // ---------- DTOs ----------
    public record ResultCategoryNumber(Map<String, Double> values, Map<String, String> colors) {
    }

    public record SegmentRow(int begin, int end, String category, String type) {
    }
}
