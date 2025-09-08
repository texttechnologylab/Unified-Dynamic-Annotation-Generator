package uni.textimager.sandbox.api.Repositories;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import static org.jooq.impl.DSL.*;

@Repository
public class VisualisationsRepository {

    private final DSLContext dsl;

    private final Table<?> V = table(name("VISUALIZATIONJSONS")).as("v");
    private final Field<String> PIPELINEID = field(name("v", "PIPELINEID"), String.class);
    private final Field<String> JSONSTR = field(name("v", "JSONSTR"), String.class);

    public VisualisationsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public java.util.Optional<String> loadJsonByPipelineId(String pipelineId) {
        return dsl.select(JSONSTR)
                .from(V)
                .where(PIPELINEID.eq(pipelineId))
                .limit(1)
                .fetchOptional(JSONSTR);
    }

    public boolean exists(String pipelineId) {
        return dsl.fetchExists(dsl.selectOne().from(V).where(PIPELINEID.eq(pipelineId)));
    }

    /**
     * Create only. Throws DuplicateKeyException if already exists (requires UNIQUE on PIPELINEID).
     */
    public void insertNew(String pipelineId, String json) {
        try {
            dsl.insertInto(V)
                    .columns(PIPELINEID, JSONSTR)
                    .values(pipelineId, json)
                    .execute();
        } catch (DataAccessException e) {
            // Best-effort mapping to 409 conflict; alternatively check exists() beforehand.
            throw new DuplicateKeyException("Pipeline already exists: " + pipelineId, e);
        }
    }

    /**
     * Replace only. Returns true if updated, false if missing.
     */
    public boolean replaceExisting(String pipelineId, String json) {
        int updated = dsl.update(V)
                .set(JSONSTR, json)
                .where(PIPELINEID.eq(pipelineId))
                .execute();
        return updated > 0;
    }
}
