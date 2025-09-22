package uni.textimager.sandbox.sources;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.database.DBConstants;
import uni.textimager.sandbox.database.QueryHelper;
import uni.textimager.sandbox.generators.Generator;
import uni.textimager.sandbox.pipeline.JSONView;
import uni.textimager.sandbox.pipeline.Pipeline;
import uni.textimager.sandbox.pipeline.PipelineNode;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.database-generator.enabled", havingValue = "true", matchIfMissing = true)
public class SourceBuilder implements ApplicationRunner {

    private final DataSource dataSource;

    public SourceBuilder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException, IOException {
        Pipeline pipeline = Pipeline.fromJSON("pipelines/pipelineUseCase2.json");
        System.out.println("Pipeline loaded: " + pipeline.getId());
        dbSavePipelinesVisualizationsJSONs(List.of(pipeline));
        dbBuildCustomTypes(pipeline);
        dbBuildGeneratorTables();
        DBAccess dbAccess = new DBAccess(dataSource);
        Collection<Generator> generators = pipeline.generateGenerators(dbAccess);
        for (Generator g : generators) {
            g.saveToDB(dbAccess);
        }
    }

    private void dbSavePipelinesVisualizationsJSONs(Collection<Pipeline> pipelines) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            dsl.createTableIfNotExists(DBConstants.TABLENAME_VISUALIZATIONJSONS)
                    .column(DBConstants.TABLEATTR_PIPELINEID, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_JSONSTR, org.jooq.impl.SQLDataType.CLOB.nullable(false))
                    .constraints(DSL.constraint("PK_" + DBConstants.TABLENAME_VISUALIZATIONJSONS).primaryKey(DBConstants.TABLEATTR_PIPELINEID))
                    .execute();

            for (Pipeline p : pipelines) {
                String pipelineID = p.getId();
                String visualizationsJSON = p.getRootJSONView().get("visualizations").toJson(false);

                dsl.insertInto(DSL.table(DBConstants.TABLENAME_VISUALIZATIONJSONS),
                                DSL.field(DBConstants.TABLEATTR_PIPELINEID),
                                DSL.field(DBConstants.TABLEATTR_JSONSTR))
                        .values(pipelineID, visualizationsJSON)
                        .execute();
            }
        }
    }

    private void dbBuildGeneratorTables() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            // CategoryNumber(Color)Mapping
            dsl.createTableIfNotExists(DBConstants.TABLENAME_GENERATORDATA_CATEGORYNUMBER)
                    .column(DBConstants.TABLEATTR_GENERATORID, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_FILENAME, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_CATEGORY, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_NUMBER, org.jooq.impl.SQLDataType.DOUBLE.nullable(false))
                    .execute();
            dsl.createTableIfNotExists(DBConstants.TABLENAME_GENERATORDATA_CATEGORYCOLOR)
                    .column(DBConstants.TABLEATTR_GENERATORID, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_CATEGORY, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_COLOR, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .execute();

            // TextFormatting
            dsl.createTableIfNotExists(DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR)
                    .column(DBConstants.TABLEATTR_GENERATORID, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_TYPE, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_CATEGORY, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_COLOR, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .execute();
            dsl.createTableIfNotExists(DBConstants.TABLENAME_GENERATORDATA_TEXT)
                    .column(DBConstants.TABLEATTR_GENERATORID, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_TEXT, org.jooq.impl.SQLDataType.CLOB.nullable(false))
                    .execute();
            dsl.createTableIfNotExists(DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE)
                    .column(DBConstants.TABLEATTR_GENERATORID, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_TYPE, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_STYLE, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .execute();
            dsl.createTableIfNotExists(DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS)
                    .column(DBConstants.TABLEATTR_GENERATORID, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_TYPE, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_BEGIN, org.jooq.impl.SQLDataType.INTEGER.nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_END, org.jooq.impl.SQLDataType.INTEGER.nullable(false))
                    .column(DBConstants.TABLEATTR_GENERATORDATA_CATEGORY, org.jooq.impl.SQLDataType.VARCHAR.length(DBConstants.DEFAULTSIZE_VARCHAR).nullable(false))
                    .execute();
        }
    }

    private void dbBuildCustomTypes(Pipeline pipeline) {
        for (PipelineNode customTypeNode : pipeline.getCustomTypes()) dbBuildCustomType(customTypeNode);
    }

    private void dbBuildCustomType(PipelineNode customTypeNode) {
        try {
            ArrayList<String> joinCols = null;
            try {
                JSONView joinColsView = customTypeNode.getConfig().get("settings").get("joinCols");
                if (joinColsView.isList()) {
                    ArrayList<String> foundJoinCols = new ArrayList<>();
                    for (JSONView col : joinColsView) {
                        String colStr = col.toString().toUpperCase().trim();
                        if (foundJoinCols.contains(colStr)) {
                            System.out.println("Warning: Join column " + colStr + " defined more than once in \"joinCols\" list of customType " + customTypeNode.getConfig().get("id") + " definition.");
                            continue;
                        }
                        foundJoinCols.add(colStr);
                    }
                    joinCols = foundJoinCols;
                }
            } catch (Exception ignored) {}

            String joinPreset;
            try { joinPreset = customTypeNode.getConfig().get("settings").get("joinPreset").toString();
            } catch (Exception e) {
                if (joinCols == null) { joinPreset = "begin&End"; }
                else { joinPreset = "manual"; }
            }
            JSONView subtypesView = customTypeNode.getConfig().get("contains");
            if (!subtypesView.isList()) {
                throw new IllegalArgumentException("\"contains\" must be a list of strings.");
            }
            try (Connection connection = dataSource.getConnection()) {
                DSLContext dsl = DSL.using(connection);
                ArrayList<String> subtypes = new ArrayList<>();
                Collection<Table<?>> dbTables = dsl.meta().getTables();
                for (JSONView elementView : subtypesView) {
                    String subtype = elementView.toString().toUpperCase().trim();
                    if (subtypes.contains(subtype)) {
                        System.out.println("Warning: Subtype " + subtype + " defined more than once in \"contains\" list of customType " + customTypeNode.getConfig().get("id") + " definition.");
                        continue;
                    }
                    if (dbTables.stream().noneMatch(t -> t.getName().equalsIgnoreCase(subtype))) {
                        System.out.println("Warning: Subtype " + subtype + " defined in \"contains\" list of customType " + customTypeNode.getConfig().get("id") + " doesn't exist in database.");
                        continue;
                    }
                    subtypes.add(subtype);
                }

                if (joinPreset.equalsIgnoreCase("begin&End")) {
                    dbBuildCustomType_customJoinFields(customTypeNode, subtypes, List.of("FILENAME", "SOFA", "BEGIN", "END"), dsl);

                } else if (joinPreset.equalsIgnoreCase("manual")) {
                    if (joinCols == null) {
                        throw new IllegalArgumentException("Error: No joinCols defined for manual-join customType " + customTypeNode.getConfig().get("id"));
                    }
                    if (!joinCols.contains("FILENAME")) { joinCols.add("FILENAME"); }
                    if (!joinCols.contains("SOFA")) { joinCols.add("SOFA"); }
                    dbBuildCustomType_customJoinFields(customTypeNode, subtypes, joinCols, dsl);
                }
            }
        } catch (Exception e) {
            System.out.println("There was an error creating the custom type: " + customTypeNode.toString());
        }
    }

    private void dbBuildCustomType_customJoinFields(PipelineNode customTypeNode, List<String> subtypes, List<String> joinFieldNames, DSLContext dsl) throws SQLException {
        QueryHelper q = new QueryHelper(dsl);

        String finalTableName = customTypeNode.getConfig().get("id").toString().toUpperCase();
        String outName =  finalTableName + "_RAW";

        // Load tables and join‐fields
        List<Table<?>> joinTables = new ArrayList<>();
        Map<String, List<Field<?>>> mapTableToJoinFields = new HashMap<>();
        for (String tableName : subtypes) {
            Table<?> t = q.table(tableName);
            joinTables.add(t);
            List<Field<?>> joinFields = new ArrayList<>();
            for (String f : joinFieldNames) {
                String dbName = (tableName + "_" + f).toUpperCase();
                joinFields.add(DSL.field(DSL.name(tableName, dbName)));
            }
            mapTableToJoinFields.put(tableName, joinFields);
        }

        // Build the join condition
        Condition joinCondition = DSL.trueCondition();
        for (int i = 0; i < joinTables.size() - 1; i++) {
            String L = subtypes.get(i), R = subtypes.get(i + 1);
            List<Field<?>> leftF  = mapTableToJoinFields.get(L);
            List<Field<?>> rightF = mapTableToJoinFields.get(R);
            for (int j = 0; j < leftF.size(); j++) {
                joinCondition = joinCondition.and(DSL.condition("{0} = {1}", leftF.get(j), rightF.get(j)));
            }
        }

        // Assemble the joined table
        Table<?> joined = joinTables.get(0);
        for (int i = 1; i < joinTables.size(); i++) {
            joined = joined.join(joinTables.get(i)).on(joinCondition);
        }

        // Build SELECT list into a List<SelectFieldOrAsterisk>
        List<SelectFieldOrAsterisk> selectFields = new ArrayList<>();
        selectFields.add(DSL.asterisk());

        String firstTbl = subtypes.get(0).toUpperCase();
        for (String f : joinFieldNames) {
            String colDbName = (firstTbl + "_" + f).toUpperCase();
            selectFields.add(DSL.field(DSL.name(firstTbl, colDbName)).as(f.toUpperCase()));
        }

        // Final SELECT using the list
        Select<Record> select = dsl.select(selectFields).from(joined);

        // Drop & create the output table
        dsl.dropTableIfExists(outName).execute();
        dsl.createTable(outName).as(select).execute();

        // Drop the original per‐table join-columns
        for (String tableName : subtypes) {
            String T = tableName.toUpperCase();
            String[] colsToDrop = joinFieldNames.stream().map(f -> (T + "_" + f).toUpperCase()).toArray(String[]::new);
            dsl.alterTable(outName).dropColumns(colsToDrop).execute();
        }

        // Clean the just-generated raw custom type table: remove unnecessary source-table-prefixes and add new custom-type-prefix
        dbCleanCustomTypeTable(subtypes, outName, finalTableName, dsl);
    }

    private void dbCleanCustomTypeTable(List<String> subtypes, String originalTableName, String newTableName, DSLContext dsl) throws SQLException {
        // Get column names
        List<String> columnNames = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, originalTableName, null)) {
                while (rs.next()) { columnNames.add(rs.getString("COLUMN_NAME")); }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while trying to load column names from DB", e);
        }
        if (columnNames.isEmpty()) {
            System.out.println("No columns found – does the table \"" + originalTableName + "\" exist?");
            return;
        }

        // Determine clean names and calculate how often they occur
        Map<String, String> cleanedNames = new LinkedHashMap<>();
        Map<String, Integer> nameCounts  = new HashMap<>();
        for (String original : columnNames) {
            String cleaned = original;
            for (String subtype : subtypes) {
                String prefix = subtype + "_";
                if (original.startsWith(prefix)) {
                    cleaned = original.substring(prefix.length());
                    break;
                }
            }
            cleanedNames.put(original, cleaned);
            nameCounts.put(cleaned, nameCounts.getOrDefault(cleaned, 0) + 1);
        }

        // Build jOOQ-Fields
        List<Field<?>> selectFields = new ArrayList<>();
        Table<?> t = DSL.table(DSL.name(originalTableName));
        for (String col : columnNames) {
            String cleaned = cleanedNames.get(col);
            Field<Object> f = DSL.field(DSL.name(originalTableName, col), Object.class);
            if (!cleaned.equals(col) && nameCounts.get(cleaned) == 1) {
                selectFields.add(f.as(newTableName + "_" + cleaned));
            } else {
                selectFields.add(f.as(newTableName + "_" + col));
            }
        }

        // Create table (DROP & CREATE AS SELECT)
        dsl.transaction(cfg -> {
            DSLContext tx = DSL.using(cfg);
            tx.dropTableIfExists(DSL.name(newTableName)).execute();
            tx.createTable(DSL.name(newTableName)).as(tx.select(selectFields).from(t)).execute();
        });
    }
}
