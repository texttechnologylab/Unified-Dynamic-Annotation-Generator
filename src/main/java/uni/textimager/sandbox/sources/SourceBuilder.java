package uni.textimager.sandbox.sources;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.database.QueryHelper;
import uni.textimager.sandbox.pipeline.Pipeline;
import uni.textimager.sandbox.pipeline.PipelineNode;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

// @Component
// @ConditionalOnProperty(name = "app.database-generator.enabled", havingValue = "true", matchIfMissing = true)
public class SourceBuilder implements ApplicationRunner {

    private final DataSource dataSource;

    public SourceBuilder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException, IOException {
        Pipeline pipeline = Pipeline.fromJSON("pipelines/pipeline1.json");
        System.out.println("Pipeline loaded: " + pipeline.getName());
        Collection<PipelineNode> relevantGenerators = pipeline.getGenerators().values();
        for (PipelineNode sourceNode : pipeline.getSources().values()) {
            Source source = new Source(sourceNode.getConfig(), relevantGenerators);
            System.out.println("Source created: " + source.getConfig().get("name"));
        }

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
