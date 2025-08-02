package uni.textimager.sandbox.sources;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.database.QueryHelper;
import uni.textimager.sandbox.generators.CategoryNumberMapping;
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

@Component
@ConditionalOnProperty(name = "app.database-generator.enabled", havingValue = "true", matchIfMissing = true)
public class SourceBuilder implements ApplicationRunner {

    private final DataSource dataSource;

    public SourceBuilder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException, IOException {
        Pipeline pipeline = Pipeline.fromJSON("pipelines/pipelineExample1.json");
        System.out.println("Pipeline loaded: " + pipeline.getName());
        DBAccess dbAccess = new DBAccess(dataSource);
        dbBuildCustomTypes(pipeline);
        Map<String, PipelineNode> relevantGenerators = pipeline.getFilteredGenerators();
        for (PipelineNode sourceNode : pipeline.getSources().values()) {
            Source source = new Source(dbAccess, sourceNode.getConfig(), relevantGenerators, sourceNode.getChildren());
            System.out.println("Source created: " + source.getConfig().get("name"));
            List<uni.textimager.sandbox.generators.Generator> list = source.createGenerators();
            System.out.println("Generators created: " + list.size());
            try {
                CategoryNumberMapping test = (CategoryNumberMapping) list.get(0);
                System.out.println(test.generateJSONCategoricalChart());
            } catch (Exception ignored) {}
            try {
                CategoryNumberMapping test = (CategoryNumberMapping) list.get(1);
                System.out.println(test.generateJSONCategoricalChart());
            } catch (Exception ignored) {}

        }
//
//        DSLContext create = DSL.using(dataSource.getConnection());
//        QueryHelper q = new QueryHelper(create);
//
//        Table<?> pos = q.table("POS");
//        Field<Object> coarse = q.field("pos", "coarsevalue");
//        Field<Integer> count = DSL.count();
//
//        Result<? extends Record> result = q.dsl()
//                .select(coarse, count)
//                .from(pos)
//                .groupBy(coarse)
//                .fetch();
//
//        result.forEach(record -> {
//            System.out.println("COARSEVALUE: " + record.getValue(coarse));
//            System.out.println("COUNT: " + record.getValue(count));
//            System.out.println("-----");
//        });

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
                            System.out.println("Warning: Join column " + colStr + " defined more than once in \"joinCols\" list of customType " + customTypeNode.getConfig().get("name") + " definition.");
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
            DSLContext dslContext = DSL.using(dataSource.getConnection());
            ArrayList<String> subtypes = new ArrayList<>();
            Collection<Table<?>> dbTables = dslContext.meta().getTables();
            for (JSONView elementView : subtypesView) {
                String subtype = elementView.toString().toUpperCase().trim();
                if (subtypes.contains(subtype)) {
                    System.out.println("Warning: Subtype " + subtype + " defined more than once in \"contains\" list of customType " + customTypeNode.getConfig().get("name") + " definition.");
                    continue;
                }
                if (dbTables.stream().noneMatch(t -> t.getName().equalsIgnoreCase(subtype))) {
                    System.out.println("Warning: Subtype " + subtype + " defined in \"contains\" list of customType " + customTypeNode.getConfig().get("name") + " doesn't exist in database.");
                    continue;
                }
                subtypes.add(subtype);
            }

//            SourceJoinType joinType;
//            try {
//                String joinTypeStr = customTypeNode.getConfig().get("settings").get("joinType").toString();
//                if (joinTypeStr.equalsIgnoreCase("inner")) {
//                    joinType = SourceJoinType.INNER;
//                } else if (joinTypeStr.equalsIgnoreCase("fullOuter")) {
//                    joinType = SourceJoinType.FULL_OUTER;
//                } else {
//                    System.out.println("Warning: Unknown joinType \"" + joinTypeStr + "\" defined in customType " + customTypeNode.getConfig().get("name") + ". Defaulting to inner.");
//                    joinType = SourceJoinType.INNER; // Default Join Type
//                }
//            } catch (Exception ignored) {
//                joinType = SourceJoinType.INNER; // Default Join Type
//            }

            if (joinPreset.equalsIgnoreCase("begin&End")) {
                dbBuildCustomType_customJoinFields(customTypeNode, subtypes, List.of("FILENAME", "SOFA", "BEGIN", "END"));

            } else if (joinPreset.equalsIgnoreCase("manual")) {
                if (joinCols == null) {
                    throw new IllegalArgumentException("Error: No joinCols defined for manual-join customType " + customTypeNode.getConfig().get("name"));
                }
                if (!joinCols.contains("FILENAME")) { joinCols.add("FILENAME"); }
                if (!joinCols.contains("SOFA")) { joinCols.add("SOFA"); }
                dbBuildCustomType_customJoinFields(customTypeNode, subtypes, joinCols);
            }

        } catch (Exception e) {
            System.out.println("There was an error creating the custom type: " + customTypeNode.toString());
        }
    }

    private void dbBuildCustomType_customJoinFields(PipelineNode customTypeNode, List<String> subtypes, List<String> joinFieldNames) throws SQLException {
        Connection connection = dataSource.getConnection();
        DSLContext dsl = DSL.using(connection);
        QueryHelper q = new QueryHelper(dsl);

        String finalTableName = customTypeNode.getConfig().get("name").toString().toUpperCase();
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
        dbCleanCustomTypeTable(subtypes, outName, finalTableName, connection);
    }

    private void dbCleanCustomTypeTable(List<String> subtypes, String originalTableName, String newTableName, Connection connection) throws SQLException {
        DSLContext dsl = DSL.using(connection);

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

        connection.close();
    }
}
