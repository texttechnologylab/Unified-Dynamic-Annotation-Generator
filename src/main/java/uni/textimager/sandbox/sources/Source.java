package uni.textimager.sandbox.sources;

import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import uni.textimager.sandbox.database.QueryHelper;
import uni.textimager.sandbox.generators.*;
import uni.textimager.sandbox.pipeline.JSONView;
import uni.textimager.sandbox.pipeline.PipelineNode;
import uni.textimager.sandbox.pipeline.PipelineNodeType;


import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;


@Getter
public class Source implements SourceInterface {

    public static final String DEFAULT_TYPE_POS = "POS";
    public static final String DEFAULT_TYPE_LEMMA = "Lemma";


    private final JSONView config;
    private final Map<String, PipelineNode> relevantGenerators;
    private final Map<String, PipelineNode> generatorsToBuild;
    private final String name;
    private final String type;
    private final String annotationTypeName;
    private final Map<String, String> featureNames;
    private final Collection<String> sourceFiles;
    private final DBAccess dbAccess;



    public Source(DBAccess dbAccess, JSONView config, Map<String, PipelineNode> relevantGenerators, Map<String, PipelineNode> generatorsToBuild) throws SQLException {
        this.dbAccess = dbAccess;
        this.config = config;
        this.relevantGenerators = relevantGenerators;
        this.generatorsToBuild = generatorsToBuild;

        this.name = config.get("name").toString();

        // Set Type and Annotation Type Name (they are the same by default)
        this.type = config.get("type").toString();
        String annotationTypeName;
        try {
            annotationTypeName = config.get("settings").get("annotationTypeName").toString();
        } catch (Exception e) {
            annotationTypeName = this.type;
        }
        this.annotationTypeName = annotationTypeName;

        // Set default feature names
        this.featureNames = AnnotationFeatures.featureNames_default(new HashMap<>());

        // Replace default feature names with provided ones
        this.featureNames.putAll(configGetOverriddenFeatureNames());

        // Initialize source files
        Collection<String> sourceFilesWhitelist = configGetSourceFiles("sourceFilesWhitelist");
        Collection<String> sourceFilesBlacklist = configGetSourceFiles("sourceFilesBlacklist");
        // If no source files are provided, use all files from the database
        if (sourceFilesWhitelist.isEmpty()) {
            sourceFilesWhitelist.addAll(dbGetAllSourceFiles());
        }
        // Remove blacklisted files from the whitelist
        sourceFilesWhitelist.removeAll(sourceFilesBlacklist);
        this.sourceFiles = sourceFilesWhitelist;
    }

    // Don't leave out filtered generators that are part of a combi with at least one relevant generator to keep visualization results consistent
    @Override
    public <T extends Generator> Collection<T> createGenerators() throws SQLException {
        // Iterate through all generators, skip combi for now.
        // Run the function createGeneratorsFromPipelineNodes for each generator

        ArrayList<T> generators = new ArrayList<>();

        JSONView createsGenerators = config.get("createsGenerators");
        for (JSONView generatorEntry : createsGenerators) {
            String generatorType = generatorEntry.get("type").toString();
            if (generatorType.equals("combi")) {
                // Skip combi for now, we will handle it later TODO: Implement combi handling
                continue;
            }
            if (!relevantGenerators.containsKey(generatorEntry.get("name").toString())) {
                System.out.println("Skipping irrelevant generator: " + generatorEntry.get("name"));
                continue;
            }
            PipelineNode generatorNode = generatorsToBuild.get(generatorEntry.get("name").toString());
            generators.addAll(createGenerators(List.of(generatorNode), generatorEntry));
        }

        return generators;
    }

    // Recursive if wanted combi is not defined TODO: Print warning in that case
    private <T extends Generator> Collection<T> createGenerators(Collection<PipelineNode> generators, JSONView config) throws SQLException {
        if (generators.isEmpty()) {
            System.out.println("Warning: empty generator collection for source: " + name);
            return List.of();
        }
        if (!containsOnlyGenerators(generators)) {
            throw new IllegalArgumentException("Source " + name + " contains non-generator nodes, returning empty collection.");
        }
        if (generators.size() == 1) {
            PipelineNode generator = generators.iterator().next();
            String generatorType = generator.getConfig().get("type").toString();
            HashMap<String, Double> categoryCountMap;
            switch (generatorType) {
                case "CategoryNumberMapping", "CategoryNumberColorMapping":
                    categoryCountMap = dbCreateCategoryCountMap(generateFeatureNameCategory());
                    if (generatorType.equals("CategoryNumberColorMapping")) {
                        return List.of((T) new CategoryNumberColorMapping(categoryCountMap));
                    } else {
                        return List.of((T) new CategoryNumberMapping(categoryCountMap));
                    }
                case "SubstringCategoryMapping", "SubstringColorMapping":
                    String configSofaFile = null;
                    String configSofaID = null;
                    try { configSofaFile = config.get("settings").get("sofaFile").toString(); } catch (Exception ignored) {}
                    try { configSofaID = config.get("settings").get("sofaID").toString(); } catch (Exception ignored) {}
                    String[] sofa = dbGetSofa(configSofaFile, configSofaID);
                    String sofaFile = sofa[0];
                    String sofaID = sofa[1];
                    String sofaString = sofa[2];

                    String featureNameCategory = generateFeatureNameCategory();
                    if (generatorType.equals("SubstringColorMapping")) {
                        categoryCountMap = dbCreateCategoryCountMap(featureNameCategory);
                        HashMap<String, Color> categoryColorMap = new CategoryNumberColorMapping(categoryCountMap).getCategoryColorMap(); // Dummy generator, use it to give more basic colors to more common categories
                        return List.of((T) new SubstringColorMapping(sofaString, dbCreateColoredSubstrings(featureNameCategory, sofaFile, sofaID, categoryColorMap)));
                    } else {
                        return List.of((T) new SubstringCategoryMapping(sofaString, dbCreateCategorizedSubstrings(featureNameCategory, sofaFile, sofaID)));
                    }
                default:
                    throw new IllegalArgumentException("Unknown generator type: " + generator.getConfig().get(type) + " for source: " + name);
            }
        }
        return List.of();
    }

    private String generateFeatureNameCategory() {
        return switch (type) {
            case DEFAULT_TYPE_POS -> featureNames.get("coarseValue");
            case DEFAULT_TYPE_LEMMA -> featureNames.get("value");
            default -> featureNames.get("value");
        };
    }


    private ArrayList<SubstringColorMapping.ColoredSubstring> dbCreateColoredSubstrings(String featureNameCategory, String sofaFile, String sofaID, HashMap<String, Color> categoryColorMap) throws SQLException {
        ArrayList<SubstringCategoryMapping.CategorizedSubstring> categorizedSubstrings = dbCreateCategorizedSubstrings(featureNameCategory, sofaFile, sofaID);
        ArrayList<SubstringColorMapping.ColoredSubstring> coloredSubstrings = new ArrayList<>();
        for (SubstringCategoryMapping.CategorizedSubstring s : categorizedSubstrings) {
            coloredSubstrings.add(new SubstringColorMapping.ColoredSubstring(s.getBegin(), s.getEnd(), categoryColorMap.get(s.getCategory())));
        }
        return coloredSubstrings;
    }

    private ArrayList<SubstringCategoryMapping.CategorizedSubstring> dbCreateCategorizedSubstrings(String featureNameCategory, String sofaFile, String sofaID) throws SQLException {
        DSLContext create = DSL.using(dbAccess.getDataSource().getConnection());
        QueryHelper q = new QueryHelper(create);

        Table<?> table            = q.table(annotationTypeName);
        Field<Object> category    = q.field(annotationTypeName, featureNameCategory);
        Field<Object> begin       = q.field(annotationTypeName, featureNames.get("begin"));
        Field<Object> end         = q.field(annotationTypeName, featureNames.get("end"));
        Field<Object> filename    = q.field(annotationTypeName, "filename");
        Field<Object> sofa        = q.field(annotationTypeName, featureNames.get("sofa"));

        Result<? extends Record> result = q.dsl()
                .select(category, begin, end)
                .from(table)
                .where(filename.equalIgnoreCase(sofaFile)
                        .and(sofa.eq(sofaID)))
                .fetch();

        ArrayList<SubstringCategoryMapping.CategorizedSubstring> categorizedSubstrings = new ArrayList<>();
        for (Record record : result) {
            int substringBegin = record.get(begin, Integer.class);
            int substringEnd = record.get(end, Integer.class);
            String substringCategory = record.get(category, String.class);

            categorizedSubstrings.add(new SubstringCategoryMapping.CategorizedSubstring(substringBegin, substringEnd, substringCategory));
        }

        return categorizedSubstrings;
    }



    private HashMap<String, Double> dbCreateCategoryCountMap(String featureNameCategory) throws SQLException {
        DSLContext create = DSL.using(dbAccess.getDataSource().getConnection());
        QueryHelper q = new QueryHelper(create);

        Table<?> table = q.table(annotationTypeName);
        Field<Object> category = q.field(annotationTypeName, featureNameCategory);
        Field<Object> filename = q.field(annotationTypeName, "filename");
        Field<Integer> count = DSL.count();

        Result<? extends Record> result = q.dsl()
                .select(category, count)
                .from(table)
                .where(filename.in(sourceFiles))
                .groupBy(category)
                .fetch();

        HashMap<String, Double> categoryCountMapping = new HashMap<>();
        result.forEach(record -> categoryCountMapping.put(record.getValue(category).toString(), record.getValue(count).doubleValue()));
        return categoryCountMapping;
    }

    private String[] dbGetSofa(String sofaFile, String sofaID) throws SQLException {
        if (sofaFile != null) sofaFile = sofaFile.trim();
        if (sofaID != null) sofaID = sofaID.trim();
        String sourceFile = "";
        if (sourceFiles.size() == 1) {
            sourceFile = sourceFiles.iterator().next();
            if (sofaFile != null && !sourceFile.equalsIgnoreCase(sofaFile)) {
                System.out.println("Warning: User-entered sofaFile " + sofaFile + " does not exist in this source, choosing source's only source-file " + sourceFile + " instead.");
            }
        } else if (sourceFiles.size() > 1) {
            String search = sofaFile;
            if (sourceFiles.stream().anyMatch(s -> s.equalsIgnoreCase(search))) {
                sourceFile = sofaFile;
            } else {
                throw new IllegalArgumentException("User-entered sofaFile " + sofaFile + " does not exist in this source. Can't decide on SOFA as this source has multiple source-files.");
            }
        }
        // From here we have a sourceFile for our SOFA.
        DSLContext create = DSL.using(dbAccess.getDataSource().getConnection());
        QueryHelper q = new QueryHelper(create);

        Table<?> table = q.table("SOFA");
        Field<Object> sofastring = q.field("SOFA", "sofastring");
        Field<Object> filename = q.field("SOFA", "filename");

        List<Object> sofastringList = q.dsl()
                .select(sofastring)
                .from(table)
                .where(filename.equalIgnoreCase(sourceFile))
                .fetch(sofastring);

        String sofaString = null;
        if (sofastringList.isEmpty()) {
            throw new IllegalArgumentException("No SOFA found in database for file " + sourceFile);
        } else if (sofastringList.size() == 1) {
            sofaString = (String) sofastringList.iterator().next();
        } else {
            // TODO: Handle multiple SOFAs in one file if sofaID is set
        }

        return new String[] {sourceFile, "2", sofaString}; // TODO: don't hardcode sofaID, but use the one from the config if it exists (or find it if there is only one)
    }

    private boolean containsOnlyGenerators(Collection<PipelineNode> nodes) {
        return nodes.stream().allMatch(node -> node.getType() == PipelineNodeType.GENERATOR);
    }

    private Map<String, String> configGetOverriddenFeatureNames() {
        try {
            JSONView featureNames = config.get("featureNames");
            if (featureNames.isMap()) {
                Map<?, ?> map = featureNames.asMap();
                boolean allStrings = map.keySet().stream().allMatch(k -> k instanceof String)
                        && map.values().stream().allMatch(v -> v instanceof String);
                if (allStrings) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> stringMap = (Map<String, String>) map;
                    return stringMap;
                }
            }
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private Collection<String> configGetSourceFiles(String key) {
        try {
            JSONView sourceFiles = config.get("settings").get(key);
            if (sourceFiles.isList()) {
                List<?> list = sourceFiles.asList();
                boolean allStrings = list.stream().allMatch(item -> item instanceof String);
                if (allStrings) {
                    @SuppressWarnings("unchecked")
                    List<String> stringList = (List<String>) list;
                    return stringList;
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Collection<String> dbGetAllSourceFiles() throws SQLException {
        return dbAccess.getSourceFiles();
    }


    public static void main(String[] args) {
        // Source s = new Source();
        // CategoryNumberMapping m = s.createGenerator(CategoryNumberMapping.class);
    }
}
