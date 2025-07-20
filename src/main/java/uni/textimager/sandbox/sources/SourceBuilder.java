package uni.textimager.sandbox.sources;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.database.QueryHelper;
import uni.textimager.sandbox.generators.Generator;
import uni.textimager.sandbox.pipeline.Pipeline;
import uni.textimager.sandbox.pipeline.PipelineNode;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.database-generator.enabled", havingValue = "true", matchIfMissing = true)
public class SourceBuilder implements ApplicationRunner {

    private final DataSource dataSource;

    public SourceBuilder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException, IOException {
        Pipeline pipeline = Pipeline.fromJSON("pipelines/pipeline2.json");
        System.out.println("Pipeline loaded: " + pipeline.getName());
        DBAccess dbAccess = new DBAccess(dataSource);
        Map<String, PipelineNode> relevantGenerators = pipeline.getFilteredGenerators();
        for (PipelineNode sourceNode : pipeline.getSources().values()) {
            Source source = new Source(dbAccess, sourceNode.getConfig(), relevantGenerators, sourceNode.getChildren());
            System.out.println("Source created: " + source.getConfig().get("name"));
            ArrayList<Generator> list = (ArrayList<Generator>) source.createGenerators();
            System.out.println("Generators created: " + list.size());
        }

        DSLContext create = DSL.using(dbAccess.getDataSource().getConnection());
        QueryHelper q = new QueryHelper(create);

        Table<?> pos = q.table("POS");
        Field<Object> coarse = q.field("pos", "coarsevalue");
        Field<Integer> count = DSL.count();

        Result<? extends Record> result = q.dsl()
                .select(coarse, count)
                .from(pos)
                .groupBy(coarse)
                .fetch();

        result.forEach(record -> {
            System.out.println("COARSEVALUE: " + record.getValue(coarse));
            System.out.println("COUNT: " + record.getValue(count));
            System.out.println("-----");
        });

//        Table<?> pos = q.table("pos");
//        Field<Object> begin = q.field("pos", "begin");
//        Field<Object> end = q.field("pos", "end");
//        Field<Object> coarse = q.field("pos", "coarsevalue");
//        Field<Object> file = q.field("pos", "filename");
//
//        Result<? extends Record> result = q.dsl()
//                .select(begin, end, coarse, file)
//                .from(pos)
//                .where(file.eq("ID21200100.xmi"))
//                .fetch();
//
//        result.forEach(record -> {
//            System.out.println("BEGIN: " + record.getValue(begin));
//            System.out.println("END: " + record.getValue(end));
//            System.out.println("COARSEVALUE: " + record.getValue(coarse));
//            System.out.println("FILENAME: " + record.getValue(file));
//            System.out.println("-----");
//        });

    }
}
