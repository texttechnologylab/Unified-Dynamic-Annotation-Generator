package uni.textimager.sandbox.api.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uni.textimager.sandbox.generators.Generator;
import uni.textimager.sandbox.pipeline.Pipeline;
import uni.textimager.sandbox.sources.DBAccess;
import uni.textimager.sandbox.sources.SourceBuildOps;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class SourceBuildService {

    private static final Logger logger = LoggerFactory.getLogger(SourceBuildService.class);
    private final DataSource dataSource;
    private final SourceBuildOps ops;

    /**
     * Build all sources for a given schema + pipeline.
     * This version runs synchronously and is not concurrency-guarded.
     */
    public void startBuild(String schema, @Nullable String pipelineId) {
        try {
            doBuild(schema, pipelineId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.warn("Build failed for pipeline={}", pipelineId);
        }
    }

    private void doBuild(String schema, @Nullable String pipelineId) throws Exception {
        if (pipelineId == null || pipelineId.isBlank()) {
            pipelineId = "main";
        }

        // Load pipeline from DB
        Pipeline pipeline = Pipeline.fromDB(dataSource, pipelineId);
        String id = pipeline.getId();

        // Persist visualization JSONs and build types/tables
        Collection<Pipeline> coll = new ArrayList<>();
        coll.add(pipeline);
        ops.savePipelinesVisualizationsJSONs(coll, schema);
        ops.buildCustomTypes(pipeline, schema);
        ops.buildGeneratorTables(schema);

        // Generate & save generator data
        DBAccess dbAccess = new DBAccess(dataSource, schema);
        Collection<Generator> generators = pipeline.generateGenerators(dbAccess);
        for (Generator g : generators) {
            g.saveToDB(dbAccess);
        }

        logger.info("Build completed for schema=" + schema + ", pipeline=" + id);
    }
}
