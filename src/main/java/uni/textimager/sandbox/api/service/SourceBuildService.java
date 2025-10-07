package uni.textimager.sandbox.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uni.textimager.sandbox.generators.Generator;
import uni.textimager.sandbox.pipeline.Pipeline;
import uni.textimager.sandbox.sources.DBAccess;
import uni.textimager.sandbox.sources.SourceBuildOps;

import javax.sql.DataSource;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class SourceBuildService {

    private final DataSource dataSource;
    private final SourceBuildOps ops;

    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public boolean startBuild(@Nullable String pipelinePath) {
        if (!lock.tryLock()) return false;
        if (!running.compareAndSet(false, true)) {
            lock.unlock();
            return false;
        }
        try {
            doBuild(pipelinePath);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Source build failed", e);
        } finally {
            running.set(false);
            lock.unlock();
        }
    }

    private void doBuild(@Nullable String pipelineId) throws Exception {

        if (pipelineId == null || pipelineId.isBlank()) {
            pipelineId = "pipelineUseCase2";
        }

        Pipeline pipeline = Pipeline.fromDB(dataSource, pipelineId);

        Collection<Pipeline> coll = new ArrayList<>();
        coll.add(pipeline);
        ops.savePipelinesVisualizationsJSONs(coll);
        ops.buildCustomTypes(pipeline);
        ops.buildGeneratorTables();

        DBAccess dbAccess = new DBAccess(dataSource, "public");
        Collection<Generator> generators = pipeline.generateGenerators(dbAccess);
        for (Generator g : generators) g.saveToDB(dbAccess);
    }

    public boolean isRunning() {
        return running.get();
    }
}
