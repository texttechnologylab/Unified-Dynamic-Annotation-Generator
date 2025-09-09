package uni.textimager.sandbox.api.Repositories;

import org.jooq.*;
import org.springframework.stereotype.Repository;
import uni.textimager.sandbox.database.DBConstants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.jooq.impl.DSL.*;

@Repository
public class GeneratorDataRepository {

    private final DSLContext dsl;

    public GeneratorDataRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<BarPieRow> loadBarPie(
            String generatorId,
            Set<String> labels,
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
        Field<String> C_GENERATORID = field(name("c", DBConstants.TABLEATTR_GENERATORID), String.class);
        Field<String> C_CATEGORY = field(name("c",DBConstants.TABLEATTR_GENERATORDATA_CATEGORY), String.class);
        Field<String> C_COLOR = field(name("c", DBConstants.TABLEATTR_GENERATORDATA_COLOR), String.class);

        Field<String> LABEL = N_CATEGORY.as("label");
        Field<BigDecimal> VALUE = sum(N_NUMBER).as("value");
        Field<String> COLOR = coalesce(max(C_COLOR), inline("#999999")).as("color");

        Condition where = N_GENERATORID.eq(generatorId);
        if (labels != null && !labels.isEmpty()) {
            where = where.and(N_CATEGORY.in(labels));
        }

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

    public record BarPieRow(String label, double value, String color) {
    }
}
