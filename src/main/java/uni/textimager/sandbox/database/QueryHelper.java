package uni.textimager.sandbox.database;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class QueryHelper {
    private final DSLContext ctx;

    public QueryHelper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Table<?> table(String name) {
        return DSL.table(DSL.name(NameMapper.mapTable(name)));
    }

    public <T> Field<T> field(String table, String field, Class<T> type) {
        return DSL.field(DSL.name(NameMapper.mapField(table, field)), type);
    }

    public Field<Object> field(String table, String field) {
        return DSL.field(DSL.name(NameMapper.mapField(table, field)));
    }

    public DSLContext dsl() {
        return ctx;
    }
}

