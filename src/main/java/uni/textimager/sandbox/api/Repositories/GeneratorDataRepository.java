package uni.textimager.sandbox.api.Repositories;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import uni.textimager.sandbox.database.DBConstants;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jooq.impl.DSL.*;

@Repository
public class GeneratorDataRepository {

    private final DSLContext dsl;
    private final String schema = "public"; // or inject @Value("${app.db.schema:public}")

    // --- tables (schema-qualified, quoted)
    private final Table<?> T_TEXT  = DSL.table(DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TEXT));
    private final Table<?> T_STYLE = DSL.table(DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE));
    private final Table<?> T_COLOR = DSL.table(DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR));
    private final Table<?> T_SEGS  = DSL.table(DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS));

    // --- fields (bind each to its OWN table)
    // TEXT
    private final Field<String> TEXT_GENERATORID = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TEXT, DBConstants.TABLEATTR_GENERATORID), String.class);
    private final Field<String> A_TEXT = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TEXT, DBConstants.TABLEATTR_GENERATORDATA_TEXT), String.class);

    // TYPESTYLE
    private final Field<String> STYLE_GENERATORID = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE, DBConstants.TABLEATTR_GENERATORID), String.class);
    private final Field<String> STYLE_TYPE = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE, DBConstants.TABLEATTR_GENERATORDATA_TYPE), String.class);
    private final Field<String> A_STYLE = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE, DBConstants.TABLEATTR_GENERATORDATA_STYLE), String.class);

    // TYPECATEGORYCOLOR
    private final Field<String> COLOR_GENERATORID = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR, DBConstants.TABLEATTR_GENERATORID), String.class);
    private final Field<String> COLOR_TYPE = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR, DBConstants.TABLEATTR_GENERATORDATA_TYPE), String.class);
    private final Field<String> COLOR_CATEGORY = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR, DBConstants.TABLEATTR_GENERATORDATA_CATEGORY), String.class);
    private final Field<String> A_COLOR = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR, DBConstants.TABLEATTR_GENERATORDATA_COLOR), String.class);

    // TYPESEGMENTS
    private final Field<String> SEGS_GENERATORID = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS, DBConstants.TABLEATTR_GENERATORID), String.class);
    private final Field<String> SEGS_TYPE = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS, DBConstants.TABLEATTR_GENERATORDATA_TYPE), String.class);
    private final Field<Integer> A_BEGIN = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS, DBConstants.TABLEATTR_GENERATORDATA_BEGIN), Integer.class);
    private final Field<Integer> A_END = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS, DBConstants.TABLEATTR_GENERATORDATA_END), Integer.class);
    private final Field<String> SEGS_CATEGORY = DSL.field(
            DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS, DBConstants.TABLEATTR_GENERATORDATA_CATEGORY), String.class);

    public GeneratorDataRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    // -------------------------
    // Queries using these fields
    // -------------------------

    public String getText(String generatorId) {
        // Do NOT upper-case the id unless your data is stored upper-cased
        String t = dsl.select(A_TEXT)
                .from(T_TEXT)
                .where(TEXT_GENERATORID.eq(generatorId))
                .fetchOne(A_TEXT);
        return t == null ? "" : t;
    }

    /** type -> styleName (bold|underline|highlight) */
    public Map<String, String> getTypeStyles(String generatorId) {
        return dsl.select(STYLE_TYPE, A_STYLE)
                .from(T_STYLE)
                .where(STYLE_GENERATORID.eq(generatorId))
                .fetchMap(STYLE_TYPE, A_STYLE);
    }

    /** type -> (category -> color) */
    public Map<String, Map<String, String>> getTypeCategoryColors(String generatorId) {
        Map<String, Map<String, String>> out = new LinkedHashMap<>();
        dsl.select(COLOR_TYPE, COLOR_CATEGORY, A_COLOR)
                .from(T_COLOR)
                .where(COLOR_GENERATORID.eq(generatorId))
                .fetch()
                .forEach(r -> out
                        .computeIfAbsent(r.get(COLOR_TYPE), k -> new LinkedHashMap<>())
                        .put(r.get(COLOR_CATEGORY), r.get(A_COLOR))
                );
        return out;
    }

    public List<Segment> getSegments(String generatorId, Set<String> typeFilter, Set<String> categoryFilter) {
        Condition c = SEGS_GENERATORID.eq(generatorId);
        if (typeFilter != null && !typeFilter.isEmpty()) c = c.and(SEGS_TYPE.in(typeFilter));
        if (categoryFilter != null && !categoryFilter.isEmpty()) c = c.and(SEGS_CATEGORY.in(categoryFilter));

        return dsl.select(SEGS_TYPE, A_BEGIN, A_END, SEGS_CATEGORY)
                .from(T_SEGS)
                .where(c)
                .orderBy(A_BEGIN.asc(), A_END.asc())
                .fetch(r -> new Segment(r.get(SEGS_TYPE), r.get(A_BEGIN), r.get(A_END), r.get(SEGS_CATEGORY)));
    }

    public List<BarPieRow> loadBarPie(
            String generatorId,
            Set<String> labels,
            Set<String> files,
            Double min,
            Double max,
            String sortKey,
            boolean desc,
            Integer limit
    ) {
        Table<?> n = table(name(DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER)).as("n");
        Table<?> c = table(name(DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR)).as("c");

        Field<String> N_GENERATORID = field(name("n", DBConstants.TABLEATTR_GENERATORID), String.class);
        Field<String> N_CATEGORY = field(name("n", DBConstants.TABLEATTR_GENERATORDATA_CATEGORY), String.class);
        Field<BigDecimal> N_NUMBER = field(quotedName("n", DBConstants.TABLEATTR_GENERATORDATA_NUMBER), BigDecimal.class); // quoted: keyword
        Field<String> N_FILENAME = field(name("n", DBConstants.TABLEATTR_FILENAME), String.class);
        Field<String> C_GENERATORID = field(name("c", DBConstants.TABLEATTR_GENERATORID), String.class);
        Field<String> C_CATEGORY = field(name("c", DBConstants.TABLEATTR_GENERATORDATA_CATEGORY), String.class);
        Field<String> C_COLOR = field(name("c", DBConstants.TABLEATTR_GENERATORDATA_COLOR), String.class);

        Field<String> LABEL = N_CATEGORY.as("label");
        Field<BigDecimal> VALUE = sum(N_NUMBER).as("value");
        Field<String> COLOR = coalesce(max(C_COLOR), inline("#999999")).as("color");

        Condition where = N_GENERATORID.eq(generatorId);
        if (labels != null && !labels.isEmpty()) where = where.and(N_CATEGORY.in(labels));
        if (files != null && !files.isEmpty()) where = where.and(N_FILENAME.in(files));

        Condition having = noCondition();
        if (min != null) having = having.and(VALUE.ge(BigDecimal.valueOf(min)));
        if (max != null) having = having.and(VALUE.le(BigDecimal.valueOf(max)));

        SortField<?> order = "label".equalsIgnoreCase(sortKey)
                ? (desc ? LABEL.desc() : LABEL.asc())
                : (desc ? VALUE.desc() : VALUE.asc());

        SelectLimitStep<Record3<String, BigDecimal, String>> base =
                dsl.select(LABEL, VALUE, COLOR)
                        .from(n)
                        .leftJoin(c)
                        .on(C_GENERATORID.eq(N_GENERATORID))
                        .and(C_CATEGORY.eq(N_CATEGORY))
                        .where(where)
                        .groupBy(LABEL)
                        .having(having)
                        .orderBy(order);

        var finalStep = (limit != null && limit >= 0) ? base.limit(limit) : base;

        return finalStep.fetch(r -> new BarPieRow(
                r.get(LABEL),
                r.get(VALUE) == null ? 0d : r.get(VALUE).doubleValue(),
                r.get(COLOR)
        ));
    }

    // per-file grouped query (no min/max/limit here; post-process later)
    public List<BarPieFileRow> loadBarPiePerFile(String generatorId,
                                                 Set<String> labels,
                                                 Set<String> files) {
        Table<?> N = DSL.table(DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER)).as("n");
        Table<?> C = DSL.table(DSL.name(schema, DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR)).as("c");

        Field<String> N_GENERATORID = DSL.field(DSL.name("n", DBConstants.TABLEATTR_GENERATORID), String.class);
        Field<String> N_CATEGORY    = DSL.field(DSL.name("n", DBConstants.TABLEATTR_GENERATORDATA_CATEGORY), String.class);
        Field<BigDecimal> N_NUMBER  = DSL.field(DSL.name("n", DBConstants.TABLEATTR_GENERATORDATA_NUMBER), BigDecimal.class);
        Field<String> N_FILENAME    = DSL.field(DSL.name("n", DBConstants.TABLEATTR_FILENAME), String.class);

        Field<String> C_GENERATORID = DSL.field(DSL.name("c", DBConstants.TABLEATTR_GENERATORID), String.class);
        Field<String> C_CATEGORY    = DSL.field(DSL.name("c", DBConstants.TABLEATTR_GENERATORDATA_CATEGORY), String.class);
        Field<String> C_COLOR       = DSL.field(DSL.name("c", DBConstants.TABLEATTR_GENERATORDATA_COLOR), String.class);

        Field<String> FILE = N_FILENAME.as("file");
        Field<String> LABEL = N_CATEGORY.as("label");
        Field<BigDecimal> VALUE = sum(N_NUMBER).as("value");
        Field<String> COLOR = coalesce(max(C_COLOR), inline("#999999")).as("color");

        Condition where = N_GENERATORID.eq(generatorId);
        if (labels != null && !labels.isEmpty()) where = where.and(N_CATEGORY.in(labels));
        if (files != null && !files.isEmpty()) where = where.and(N_FILENAME.in(files));

        return dsl.select(FILE, LABEL, VALUE, COLOR)
                .from(N)
                .leftJoin(C).on(C_GENERATORID.eq(N_GENERATORID)).and(C_CATEGORY.eq(N_CATEGORY))
                .where(where)
                .groupBy(FILE, LABEL)
                .fetch(r -> new BarPieFileRow(
                        r.get(FILE),
                        r.get(LABEL),
                        r.get(VALUE) == null ? 0d : r.get(VALUE).doubleValue(),
                        r.get(COLOR)
                ));
    }

    public record BarPieFileRow(String file, String label, double value, String color) {
    }

    public record Segment(String type, int begin, int end, String category) {
    }

    public record BarPieRow(String label, double value, String color) {
    }
}
