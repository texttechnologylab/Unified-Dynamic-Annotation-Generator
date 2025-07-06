package uni.textimager.sandbox.sources;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.pipeline.Pipeline;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@Component
public class SourceBuilder implements ApplicationRunner {

    private final DataSource dataSource;

    public SourceBuilder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException, IOException {
        Pipeline pipeline = Pipeline.fromJSON("pipelines/pipeline1.json");
        System.out.println("Pipeline loaded: " + pipeline.getName());

        DSLContext create = DSL.using(dataSource.getConnection());

        Table<?> POS = DSL.table(DSL.name("POS"));
        Field<Object> _BEGIN = DSL.field(DSL.name("_BEGIN"));
        Field<Object> _END = DSL.field(DSL.name("_END"));
        Field<Object> COARSEVALUE = DSL.field(DSL.name("COARSEVALUE"));
        Field<Object> FILENAME = DSL.field(DSL.name("FILENAME"));

        Result<? extends Record> result = create.select(_BEGIN, _END, COARSEVALUE, FILENAME)
                .from(POS)
                .where(FILENAME.eq("ID21200100.xmi"))
                .fetch();

        result.forEach(record -> {
            System.out.println("BEGIN: " + record.getValue("_BEGIN"));
            System.out.println("END: " + record.getValue("_END"));
            System.out.println("COARSEVALUE: " + record.getValue("COARSEVALUE"));
            System.out.println("FILENAME: " + record.getValue("FILENAME"));
            System.out.println("-----");
        });

    }
}
