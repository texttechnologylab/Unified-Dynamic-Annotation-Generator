package uni.textimager.sandbox.api.Repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import uni.textimager.sandbox.database.DBConstants;

import java.util.Optional;

import static org.jooq.impl.DSL.*;

@Repository
public class VisualisationsRepository {

    private final DSLContext dsl;

    private final Table<?> V = table(name(DBConstants.TABLENAME_VISUALIZATIONJSONS)).as("v");
    private final Field<String> PIPELINEID = field(name("v", DBConstants.TABLEATTR_PIPELINEID), String.class);
    private final Field<String> JSONSTR = field(name("v", DBConstants.TABLEATTR_JSONSTR), String.class);
    private final ObjectMapper mapper = new ObjectMapper();

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

    /**
     * Lookup a single visualization meta by pipelineId + visualizationId.
     * Returns empty if not found.
     */
    public Optional<VisualizationMeta> findMeta(String pipelineId, String visualizationId) {
        String json = dsl.select(JSONSTR)
                .from(V)
                .where(PIPELINEID.eq(pipelineId))
                .fetchOne(JSONSTR);

        if (json == null) return Optional.empty();

        try {
            JsonNode root = mapper.readTree(json);
            if (root.isArray()) {
                for (JsonNode n : root) {
                    String id = n.path("id").asText(null);
                    if (visualizationId.equals(id)) {
                        String type = n.path("type").asText(null);
                        String gen = n.path("generator").path("id").asText(null);
                        if (type != null && gen != null) {
                            return Optional.of(new VisualizationMeta(id, type, gen));
                        }
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new DataAccessException("Failed to parse visualization JSON for pipeline " + pipelineId, e) {
            };
        }
    }

    /**
     * Fallback: search across all pipelines if caller does not know the pipelineId.
     * First match wins.
     */
    public Optional<VisualizationMeta> findMeta(String visualizationId) {
        return dsl.select(PIPELINEID, JSONSTR).from(V).fetchStream().map(rec -> {
            String json = rec.get(JSONSTR);
            try {
                JsonNode root = mapper.readTree(json);
                if (root.isArray()) {
                    for (JsonNode n : root) {
                        String id = n.path("id").asText(null);
                        if (visualizationId.equals(id)) {
                            String type = n.path("type").asText(null);
                            String gen = n.path("generator").path("id").asText(null);
                            if (type != null && gen != null) {
                                return Optional.of(new VisualizationMeta(id, type, gen));
                            }
                        }
                    }
                }
                return Optional.<VisualizationMeta>empty();
            } catch (Exception e) {
                return Optional.<VisualizationMeta>empty();
            }
        }).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    public record VisualizationMeta(String visualizationId, String type, String generatorId) {
    }
}
