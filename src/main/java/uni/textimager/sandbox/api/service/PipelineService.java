package uni.textimager.sandbox.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
public class PipelineService {

    private static final String TABLE = "pipeline";
    private static final String COL_ID = "pipeline_id";
    private static final String COL_NAME = "pipeline_name";
    private static final String COL_JSON = "json";
    private final SourceBuildService sourceBuildService;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public PipelineService(SourceBuildService sourceBuildService, DataSource dataSource, ObjectMapper objectMapper) {
        this.sourceBuildService = sourceBuildService;
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }

//    @PostConstruct
//    void ensureTable() throws Exception {
//        try (Connection c = dataSource.getConnection()) {
//            DSLContext dsl = DSL.using(c);
//            dsl.createTableIfNotExists(TABLE)
//                    .column(COL_NAME, SQLDataType.VARCHAR(255).nullable(false))
//                    .column(COL_JSON, SQLDataType.CLOB.nullable(false)) // switch to JSONB if on Postgres
//                    .constraints(DSL.constraint("PK_" + TABLE).primaryKey(COL_NAME))
//                    .execute();
//        }
//    }

    @Transactional(readOnly = true)
    public List<String> listIds(int page, int size, String q) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            DSLContext dsl = DSL.using(c);
            var cond = (q == null || q.isBlank())
                    ? DSL.noCondition()
                    : DSL.field(COL_ID, String.class).likeIgnoreCase("%" + q + "%");
            return dsl.select(DSL.field(COL_ID, String.class))
                    .from(DSL.table(TABLE))
                    .where(cond)
                    .orderBy(DSL.field(COL_ID).asc())
                    .offset(Math.max(0, page) * Math.max(1, size))
                    .limit(Math.max(1, size))
                    .fetchInto(String.class);
        }
    }

    @Transactional(readOnly = true)
    public JsonNode get(String id) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            DSLContext dsl = DSL.using(c);
            String json = dsl.select(DSL.field(COL_JSON, String.class))
                    .from(DSL.table(TABLE))
                    .where(DSL.field(COL_ID).eq(id))
                    .fetchOneInto(String.class);
            if (json == null) throw new ResponseStatusException(NOT_FOUND, "Pipeline not found");
            return parseJson(json);
        }
    }

    @Transactional
    public String create(JsonNode json) throws Exception {
        String id = json.get("id").asText("main");
        String jsonStr = toString(json);
        try (Connection c = dataSource.getConnection()) {
            DSLContext dsl = DSL.using(c);
            // check exists
            boolean exists = dsl.fetchExists(
                    dsl.selectOne()
                            .from(DSL.table(TABLE))
                            .where(DSL.field(COL_ID).eq(id))
            );
            if (exists) throw new ResponseStatusException(CONFLICT, "Pipeline already exists");

            dsl.insertInto(DSL.table(TABLE),
                            DSL.field(COL_ID),
                            DSL.field(COL_NAME),
                            DSL.field(COL_JSON))
                    .values(id, id, jsonStr)
                    .execute();

            sourceBuildService.startBuild(id, id);
        }

        return id;
    }

    @Transactional
    public void update(String name, JsonNode json) throws Exception {
        String jsonStr = toString(json);
        try (Connection c = dataSource.getConnection()) {
            DSLContext dsl = DSL.using(c);
            int updated = dsl.update(DSL.table(TABLE))
                    .set(DSL.field(COL_JSON), jsonStr)
                    .where(DSL.field(COL_NAME).eq(name))
                    .execute();
            if (updated == 0) throw new ResponseStatusException(NOT_FOUND, "Pipeline not found");
        }
    }

    @Transactional
    public void delete(String name) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            DSLContext dsl = DSL.using(c);
            int deleted = dsl.deleteFrom(DSL.table(TABLE))
                    .where(DSL.field(COL_NAME).eq(name))
                    .execute();
            if (deleted == 0) throw new ResponseStatusException(NOT_FOUND, "Pipeline not found");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank())
            throw new ResponseStatusException(BAD_REQUEST, "Name must not be blank");
        if (!name.matches("[A-Za-z0-9._-]{1,255}"))
            throw new ResponseStatusException(BAD_REQUEST, "Invalid name (allowed: letters, digits, ., _, -)");
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            // Stored value is not valid JSON (shouldn't happen if we always validate on write)
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Stored JSON is invalid");
        }
    }

    private String toString(JsonNode json) {
        try {
            // Normalize JSON; also rejects invalid JSON early if coming as raw string
            return (json == null) ? "{}" : objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid JSON");
        }
    }
}
