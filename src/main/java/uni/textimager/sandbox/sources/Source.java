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
        this.featureNames.putAll(configSourceGetOverriddenFeatureNames());

        // Set all source files that are used in this source
        this.sourceFiles = generateSourceFiles(config);
    }

    // Don't leave out filtered generators that are part of a combi with at least one relevant generator to keep visualization results consistent
    @Override
    public Collection<Generator> createGenerators() throws SQLException {
        ArrayList<Generator> generators = new ArrayList<>();

        JSONView createsGenerators = config.get("createsGenerators");
        for (JSONView generatorEntry : createsGenerators) {
            String generatorType = generatorEntry.get("type").toString();
            if (generatorType.equals("combi")) {
                ArrayList<PipelineNode> subGeneratorNodes = new ArrayList<>();
                boolean combiNeeded = false;
                for (JSONView subGeneratorEntry : generatorEntry.get("createsGenerators")) {
                    if (relevantGenerators.containsKey(subGeneratorEntry.get("name").toString())) combiNeeded = true;
                    subGeneratorNodes.add(generatorsToBuild.get(subGeneratorEntry.get("name").toString()));
                }
                if (!combiNeeded) {
                    String combiName = "unnamed combi";
                    try { combiName = generatorEntry.get("name").toString(); } catch (Exception ignored) {}
                    System.out.println("Skipping irrelevant combi-generator \"" + combiName + "\".");
                    continue;
                }
                generators.addAll(createGeneratorsCombi(subGeneratorNodes, generatorEntry));
            } else if (generatorType.equals("bundle")) {
                for (JSONView subGeneratorEntry : generatorEntry.get("createsGenerators")) {
                    if (!relevantGenerators.containsKey(subGeneratorEntry.get("name").toString())) {
                        System.out.println("Skipping irrelevant bundle-part generator \"" + subGeneratorEntry.get("name") + "\".");
                        continue;
                    }
                    PipelineNode generatorNode = generatorsToBuild.get(subGeneratorEntry.get("name").toString());
                    generators.add(createGenerator(generatorNode, generatorEntry, subGeneratorEntry));
                }
            } else {
                if (!relevantGenerators.containsKey(generatorEntry.get("name").toString())) {
                    System.out.println("Skipping irrelevant generator \"" + generatorEntry.get("name") + "\".");
                    continue;
                }
                PipelineNode generatorNode = generatorsToBuild.get(generatorEntry.get("name").toString());
                generators.add(createGenerator(generatorNode, null, generatorEntry));
            }

        }

        return generators;
    }


    private Collection<Generator> createGeneratorsCombi(Collection<PipelineNode> generators, JSONView configCombi) throws SQLException {
        // Step 1 - Find common traits for generators
        HashMap<String, HashMap<String, Color>> mapFeatureToCategoryColorMap = new HashMap<>();
        for (PipelineNode g : generators) {
            String generatorType = g.getConfig().get("type").toString();
            if (generatorType.equals("CategoryNumberColorMapping") || generatorType.equals("SubstringColorMapping")) {
                mapFeatureToCategoryColorMap.put(generateFeatureNameCategory(configCombi, g.getConfig()), null);
            }
        }

        // Step 2 - Generate data for common traits
        Collection<String> combiSourceFiles = generateSourceFiles(configCombi, sourceFiles);
        for (String feature : mapFeatureToCategoryColorMap.keySet()) {
            mapFeatureToCategoryColorMap.put(feature, dbCreateCategoryColorMap(feature, combiSourceFiles));
        }

        // Step 3 - Create generators using the common data
        ArrayList<Generator> combiGenerators = new ArrayList<>();
        for (PipelineNode g : generators) {
            String generatorType = g.getConfig().get("type").toString();
            if (generatorType.equals("CategoryNumberColorMapping")) {
                String featureName = generateFeatureNameCategory(configCombi, g.getConfig());
                Collection<String> generatorSourceFiles = generateSourceFiles(g.getConfig(), combiSourceFiles);
                HashMap<String, Double> categoryNumberMap = dbCreateCategoryCountMap(featureName, generatorSourceFiles);
                HashMap<String, Color> categoryColorMap = new HashMap<>(mapFeatureToCategoryColorMap.get(featureName));
                categoryColorMap.keySet().retainAll(categoryNumberMap.keySet());
                combiGenerators.add(new CategoryNumberColorMapping(categoryNumberMap, categoryColorMap));
            } else if (generatorType.equals("SubstringColorMapping")) {
                String configSofaFile = generateSofaFile(configCombi, g.getConfig());
                String configSofaID = generateSofaID(configCombi, g.getConfig());
                String[] sofa = dbGetSofa(configSofaFile, configSofaID);
                String sofaFile = sofa[0];
                String sofaID = sofa[1];
                String sofaString = sofa[2];
                String featureName = generateFeatureNameCategory(configCombi, g.getConfig());
                HashMap<String, Color> categoryColorMap = mapFeatureToCategoryColorMap.get(featureName);
                combiGenerators.add(new SubstringColorMapping(sofaString, dbCreateColoredSubstrings(featureName, sofaFile, sofaID, categoryColorMap)));
            } else { // Default case: Just treat the unknown bundle generator like a normal single generator.
                combiGenerators.add(createGenerator(g, configCombi, g.getConfig()));
            }
        }

        return combiGenerators;
    }



    private Generator createGenerator(PipelineNode generator, JSONView configBundle, JSONView config) throws SQLException {
        String generatorType = generator.getConfig().get("type").toString();
        Collection<String> sourceFiles = generateSourceFiles(configBundle, config);
        switch (generatorType) {
            case "CategoryNumberMapping", "CategoryNumberColorMapping":
                HashMap<String, Double> categoryCountMap = dbCreateCategoryCountMap(generateFeatureNameCategory(configBundle, config), sourceFiles);
                if (generatorType.equals("CategoryNumberColorMapping")) {
                    return new CategoryNumberColorMapping(categoryCountMap);
                } else {
                    return new CategoryNumberMapping(categoryCountMap);
                }
            case "SubstringCategoryMapping", "SubstringColorMapping":
                String configSofaFile = generateSofaFile(configBundle, config);
                String configSofaID = generateSofaID(configBundle, config);
                String[] sofa = dbGetSofa(configSofaFile, configSofaID);
                String sofaFile = sofa[0];
                String sofaID = sofa[1];
                String sofaString = sofa[2];

                String featureNameCategory = generateFeatureNameCategory(configBundle, config);
                if (generatorType.equals("SubstringColorMapping")) {
                    HashMap<String, Color> categoryColorMap = dbCreateCategoryColorMap(featureNameCategory, sourceFiles);
                    return new SubstringColorMapping(sofaString, dbCreateColoredSubstrings(featureNameCategory, sofaFile, sofaID, categoryColorMap));
                } else {
                    return new SubstringCategoryMapping(sofaString, dbCreateCategorizedSubstrings(featureNameCategory, sofaFile, sofaID));
                }
            default:
                throw new IllegalArgumentException("Unknown generator type: " + generator.getConfig().get("type") + " for source: " + name);
        }
    }

    private String generateFeatureNameCategory(JSONView configBundle, JSONView config) {
        try { return config.get("settings").get("featureName").toString(); } catch (Exception ignored) {}
        try { return configBundle.get("settings").get("featureName").toString(); } catch (Exception ignored) {}

        return switch (type) {
            case DEFAULT_TYPE_POS -> featureNames.get("coarseValue");
            case DEFAULT_TYPE_LEMMA -> featureNames.get("value");
            default -> featureNames.get("value");
        };
    }

    private String generateSofaFile(JSONView configBundle, JSONView config) {
        try { return config.get("settings").get("sofaFile").toString(); } catch (Exception ignored) {}
        try { return configBundle.get("settings").get("sofaFile").toString(); } catch (Exception ignored) {}
        return null;
    }

    private String generateSofaID(JSONView configBundle, JSONView config) {
        try { return config.get("settings").get("sofaID").toString(); } catch (Exception ignored) {}
        try { return configBundle.get("settings").get("sofaID").toString(); } catch (Exception ignored) {}
        return null;
    }

    private Collection<String> generateCategories(JSONView configBundle, JSONView config) {
        Collection<String> categoriesBundleWhitelist = generateCategories(configBundle, sourceFiles);
        return generateCategories(config, categoriesBundleWhitelist);
    }
    private Collection<String> generateCategories(JSONView config, Collection<String> allCategories) {
        Collection<String> categoriesWhitelist = configGetStringList(config, "sourceFilesWhitelist", false);
        Collection<String> categoriesBlacklist = configGetStringList(config, "sourceFilesBlacklist", false);

        if (categoriesWhitelist == null) {
            return null; // null means that we don't want to exclude any categories
        }

        return categoriesWhitelist;
    }

    private Collection<String> generateSourceFiles(JSONView configBundle, JSONView config) {
        Collection<String> sourceFilesBundleWhitelist = generateSourceFiles(configBundle, sourceFiles);
        return generateSourceFiles(config, sourceFilesBundleWhitelist);
    }
    private Collection<String> generateSourceFiles(JSONView config, Collection<String> allSourceFiles) {
        Collection<String> sourceFilesWhitelist = configGetSourceFiles(config, "sourceFilesWhitelist");
        Collection<String> sourceFilesBlacklist = configGetSourceFiles(config, "sourceFilesBlacklist");

        if (sourceFilesWhitelist.isEmpty()) {
            // If no source files are provided, use all files
            sourceFilesWhitelist.addAll(allSourceFiles);
        } else if (!allSourceFiles.containsAll(sourceFilesWhitelist)) {
            System.out.println("Warning: Source file whitelist contains elements that are unknown or excluded on a higher level. Removing those elements.");
            sourceFilesWhitelist.retainAll(allSourceFiles);
        }
        // Remove blacklisted files from the whitelist
        sourceFilesWhitelist.removeAll(sourceFilesBlacklist);

        return sourceFilesWhitelist;
    }

    private Collection<String> generateSourceFiles(JSONView config) throws SQLException {
        return generateSourceFiles(config, dbGetAllSourceFiles());
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


    private HashMap<String, Color> dbCreateCategoryColorMap(String featureNameCategory, Collection<String> sourceFiles) throws SQLException {
        HashMap<String, Double> categoryCountMap = dbCreateCategoryCountMap(featureNameCategory, sourceFiles);
        return new CategoryNumberColorMapping(categoryCountMap).getCategoryColorMap(); // Dummy generator, use it to give more basic colors to more common categories
    }


    private HashMap<String, Double> dbCreateCategoryCountMap(String featureNameCategory, Collection<String> sourceFiles) throws SQLException {
        if (sourceFiles == null || sourceFiles.isEmpty()) {
            if (sourceFiles != null) {
                System.out.println("Warning: Got empty source files list when trying to build a generator for feature \"" + featureNameCategory + "\". Defaulting to source files of Source object.");
            }
            sourceFiles = this.sourceFiles;
        }

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

    private Map<String, String> configSourceGetOverriddenFeatureNames() {
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
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }

    private Collection<String> configGetStringList(JSONView config, String key, boolean returnEmptyIfNotConfigured) {
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
            if (returnEmptyIfNotConfigured) {
                return new ArrayList<>();
            }
            return null;
        } catch (Exception ignored) {
            if (returnEmptyIfNotConfigured) {
                return new ArrayList<>();
            }
            return null;
        }
    }
    private Collection<String> configGetSourceFiles(JSONView config, String key) {
        return configGetStringList(config, key, true);
    }

    private Collection<String> dbGetAllSourceFiles() throws SQLException {
        return dbAccess.getSourceFiles();
    }
}
