package uni.textimager.sandbox.example;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.database.QueryHelper;

import javax.sql.DataSource;
import java.sql.SQLException;

public class QueryHelperExample implements ApplicationRunner {
    private final DataSource dataSource;

    public QueryHelperExample(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {

        DSLContext create = DSL.using(dataSource.getConnection());
        QueryHelper q = new QueryHelper(create);

        Table<?> pos = q.table("pos");
        Field<Object> begin = q.field("pos", "begin");
        Field<Object> end = q.field("pos", "end");
        Field<Object> coarse = q.field("pos", "coarsevalue");
        Field<Object> file = q.field("pos", "filename");

        Result<? extends Record> result = q.dsl()
                .select(begin, end, coarse, file)
                .from(pos)
                .where(file.eq("ID21200100.xmi"))
                .fetch();

        result.forEach(record -> {
            System.out.println("BEGIN: " + record.getValue(begin));
            System.out.println("END: " + record.getValue(end));
            System.out.println("COARSEVALUE: " + record.getValue(coarse));
            System.out.println("FILENAME: " + record.getValue(file));
            System.out.println("-----");
        });
    }
}
