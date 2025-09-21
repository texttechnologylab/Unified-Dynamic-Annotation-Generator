package uni.textimager.sandbox.sources;

import lombok.Getter;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import uni.textimager.sandbox.database.QueryHelper;
import uni.textimager.sandbox.generators.*;
import uni.textimager.sandbox.generators.Generator;
import uni.textimager.sandbox.pipeline.JSONView;
import uni.textimager.sandbox.pipeline.PipelineNode;
import uni.textimager.sandbox.pipeline.PipelineNodeType;


import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public class Source implements SourceInterface {

    public static final String DEFAULT_TYPE_POS = "POS";
    public static final String DEFAULT_TYPE_LEMMA = "Lemma";


    private final JSONView config;
    private final Map<String, PipelineNode> relevantGenerators;
    private final Map<String, PipelineNode> generatorsToBuild;
    private final String id;
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

        this.id = config.get("id").toString();

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
    public List<Generator> createGenerators() throws SQLException {
        ArrayList<Generator> generators = new ArrayList<>();

        JSONView createsGenerators = config.get("createsGenerators");
        for (JSONView generatorEntry : createsGenerators) {
            String generatorType = generatorEntry.get("type").toString();
            if (generatorType.equals("combi")) {
                ArrayList<PipelineNode> subGeneratorNodes = new ArrayList<>();
                boolean combiNeeded = false;
                for (JSONView subGeneratorEntry : generatorEntry.get("createsGenerators")) {
                    if (relevantGenerators.containsKey(subGeneratorEntry.get("id").toString())) combiNeeded = true;
                    subGeneratorNodes.add(generatorsToBuild.get(subGeneratorEntry.get("id").toString()));
                }
                if (!combiNeeded) {
                    String combiName = "unnamed combi";
                    try { combiName = generatorEntry.get("id").toString(); } catch (Exception ignored) {}
                    System.out.println("Skipping irrelevant combi-generator \"" + combiName + "\".");
                    continue;
                }
                generators.addAll(createGeneratorsCombi(subGeneratorNodes, generatorEntry));
            } else if (generatorType.equals("bundle")) {
                for (JSONView subGeneratorEntry : generatorEntry.get("createsGenerators")) {
                    if (!relevantGenerators.containsKey(subGeneratorEntry.get("id").toString())) {
                        System.out.println("Skipping irrelevant bundle-part generator \"" + subGeneratorEntry.get("id") + "\".");
                        continue;
                    }
                    PipelineNode generatorNode = generatorsToBuild.get(subGeneratorEntry.get("id").toString());
                    generators.add(createGenerator(generatorNode, generatorEntry, subGeneratorEntry));
                }
            } else {
                if (!relevantGenerators.containsKey(generatorEntry.get("id").toString())) {
                    System.out.println("Skipping irrelevant generator \"" + generatorEntry.get("id") + "\".");
                    continue;
                }
                PipelineNode generatorNode = generatorsToBuild.get(generatorEntry.get("id").toString());
                generators.add(createGenerator(generatorNode, null, generatorEntry));
            }

        }

        return generators;
    }


    private Collection<Generator> createGeneratorsCombi(Collection<PipelineNode> generators, JSONView configCombi) throws SQLException {
        // Step 1 - Find common traits for generators
        HashMap<String, Map<String, Color>> mapFeatureToCategoryColorMap = new HashMap<>();
        for (PipelineNode g : generators) {
            String generatorType = g.getConfig().get("type").toString();
            if (generatorType.equals("CategoryNumberColorMapping") || generatorType.equals("TextFormatting")) {
                mapFeatureToCategoryColorMap.put(generateFeatureNameCategory(configCombi, g.getConfig()), null);
            }
        }

        // Step 2 - Generate data for common traits
        Collection<String> combiSourceFiles = generateSourceFiles(configCombi, sourceFiles);
        Collection<String> combiCategoriesWhitelist = generateCategoriesWhitelist(configCombi, null);
        Collection<String> combiCategoriesBlacklist = generateCategoriesBlacklist(configCombi, null);
        for (String feature : mapFeatureToCategoryColorMap.keySet()) {
            mapFeatureToCategoryColorMap.put(feature, dbCreateCategoryColorMap(feature, combiSourceFiles, combiCategoriesWhitelist, combiCategoriesBlacklist));
        }

        // Step 3 - Create generators using the common data
        ArrayList<Generator> combiGenerators = new ArrayList<>();
        for (PipelineNode g : generators) {
            String generatorID = g.getConfig().get("id").toString();
            String generatorType = g.getConfig().get("type").toString();
            if (generatorType.equals("CategoryNumberColorMapping")) {
                String featureName = generateFeatureNameCategory(configCombi, g.getConfig());
                Collection<String> generatorSourceFiles = generateSourceFiles(g.getConfig(), combiSourceFiles);
                Collection<String> categoriesWhitelist = generateCategoriesWhitelist(configCombi, g.getConfig());
                Collection<String> categoriesBlacklist = generateCategoriesBlacklist(configCombi, g.getConfig());
                Map<String, Map<String, Double>> categoryNumberMap = dbCreateCategoryCountMap(featureName, generatorSourceFiles, categoriesWhitelist, categoriesBlacklist);
                Color singleColor = generateColor(configCombi, g.getConfig());
                if (singleColor == null) {
                    Map<String, Color> categoryColorMap = new HashMap<>(mapFeatureToCategoryColorMap.get(featureName));
                    categoryColorMap.keySet().retainAll(CategoryNumberMapping.calculateTotalFromCategoryCountMap(categoryNumberMap).keySet());
                    combiGenerators.add(new CategoryNumberColorMapping(generatorID, categoryNumberMap, categoryColorMap));
                } else {
                    combiGenerators.add(new CategoryNumberColorMapping(generatorID, categoryNumberMap, singleColor));
                }
            } else if (generatorType.equals("TextFormatting")) {
                String configSofaFile = generateBundleAttribute(configCombi, g.getConfig(), "sofaFile");
                String configSofaID = generateBundleAttribute(configCombi, g.getConfig(), "sofaID");
                String[] sofa = dbGetSofa(configSofaFile, configSofaID);
                String sofaFile = sofa[0];
                String sofaID = sofa[1];
                String sofaString = sofa[2];
                String featureName = generateFeatureNameCategory(configCombi, g.getConfig());
                String style = generateTextFormattingStyle(configCombi, g.getConfig());
                Color singleColor = generateColor(configCombi, g.getConfig());
                Map<String, Color> categoryColorMap;
                if (singleColor == null) {
                    categoryColorMap = mapFeatureToCategoryColorMap.get(featureName);
                } else {
                    categoryColorMap = mapFeatureToCategoryColorMap.get(featureName).keySet().stream().collect(Collectors.toMap(k -> k, k -> singleColor));
                }
                Collection<String> categoriesWhitelist = generateCategoriesWhitelist(configCombi, g.getConfig());
                Collection<String> categoriesBlacklist = generateCategoriesBlacklist(configCombi, g.getConfig());
                combiGenerators.add(dbBuildTextFormatting(featureName, sofaFile, sofaID, categoriesWhitelist, categoriesBlacklist, categoryColorMap, generatorID, sofaString, style));
            } else { // Default case: Just treat the unknown bundle generator like a normal single generator.
                combiGenerators.add(createGenerator(g, configCombi, g.getConfig()));
            }
        }

        return combiGenerators;
    }



    private Generator createGenerator(PipelineNode generator, JSONView configBundle, JSONView config) throws SQLException {
        String generatorID = generator.getConfig().get("id").toString();
        String generatorType = generator.getConfig().get("type").toString();
        Collection<String> sourceFiles = generateSourceFiles(configBundle, config);
        if (generatorType.equals("CategoryNumberMapping") || generatorType.equals("CategoryNumberColorMapping")) {
            Collection<String> categoriesWhitelist = generateCategoriesWhitelist(configBundle, config);
            Collection<String> categoriesBlacklist = generateCategoriesBlacklist(configBundle, config);
            Map<String, Map<String, Double>> categoryCountMap = dbCreateCategoryCountMap(generateFeatureNameCategory(configBundle, config), sourceFiles, categoriesWhitelist, categoriesBlacklist);
            Color singleColor = generateColor(configBundle, config);
            if (singleColor != null) {
                if (generatorType.equals("CategoryNumberColorMapping")) {
                    return new CategoryNumberColorMapping(generatorID, categoryCountMap, singleColor);
                } else {
                    return new CategoryNumberMapping(generatorID, categoryCountMap, singleColor);
                }
            } else if (generatorType.equals("CategoryNumberColorMapping")) {
                return new CategoryNumberColorMapping(generatorID, categoryCountMap);
            } else {
                return new CategoryNumberMapping(generatorID, categoryCountMap);
            }
        } else if (generatorType.equals("TextFormatting")) {
            String configSofaFile =  generateBundleAttribute(configBundle, config, "sofaFile");
            String configSofaID = generateBundleAttribute(configBundle, config, "sofaID");
            String[] sofa = dbGetSofa(configSofaFile, configSofaID);
            String sofaFile = sofa[0];
            String sofaID = sofa[1];
            String sofaString = sofa[2];

            String featureName = generateFeatureNameCategory(configBundle, config);
            String style = generateTextFormattingStyle(configBundle, config);
            Collection<String> categoriesWhitelist = generateCategoriesWhitelist(configBundle, config);
            Collection<String> categoriesBlacklist = generateCategoriesBlacklist(configBundle, config);
            Color singleColor = generateColor(configBundle, config);
            Map<String, Color> categoryColorMap = dbCreateCategoryColorMap(featureName, sourceFiles, categoriesWhitelist, categoriesBlacklist, singleColor);
            return dbBuildTextFormatting(featureName, sofaFile, sofaID, categoriesWhitelist, categoriesBlacklist, categoryColorMap, generatorID, sofaString, style);
        } else {
            throw new IllegalArgumentException("Unknown generator type: " + generator.getConfig().get("type") + " for source: " + id);
        }
    }

    private String generateFeatureNameCategory(JSONView configBundle, JSONView config) {
        String featureNameCategory = generateBundleAttribute(configBundle, config, "featureName");
        if (featureNameCategory != null) return featureNameCategory;

        return switch (type) {
            case DEFAULT_TYPE_POS -> featureNames.get("coarseValue");
            default -> featureNames.get("value");
        };
    }

    private String generateTextFormattingStyle(JSONView configBundle, JSONView config) {
        String textFormattingType = generateBundleAttribute(configBundle, config, "style");
        if (textFormattingType != null) return textFormattingType;
        return TextFormatting.DEFAULT_STYLE;
    }

    private Color generateColor(JSONView configBundle, JSONView config) {
        String colorStr = generateBundleAttribute(configBundle, config, "color");
        if (colorStr == null) return null;
        try { return Color.decode(colorStr);
        } catch (NumberFormatException ignored) {}
        return null;
    }

    private String generateBundleAttribute(JSONView configBundle, JSONView config, String attribute) {
        try { return config.get("settings").get(attribute).toString(); } catch (Exception ignored) {}
        try { return configBundle.get("settings").get(attribute).toString(); } catch (Exception ignored) {}
        return null;
    }

    private Collection<String> generateCategoriesWhitelist(JSONView configBundle, JSONView config) {
        Set<String> categoriesBundleWhitelist = configGetStringSet(configBundle, "categoriesWhitelist", false);
        Set<String> categoriesWhitelist = configGetStringSet(config, "categoriesWhitelist", false);

        if (categoriesWhitelist == null & categoriesBundleWhitelist == null) return null;
        if (categoriesWhitelist == null) return categoriesBundleWhitelist;
        if (categoriesBundleWhitelist == null) return categoriesWhitelist;
        categoriesWhitelist.retainAll(categoriesBundleWhitelist);
        return categoriesWhitelist;
    }
    private Collection<String> generateCategoriesBlacklist(JSONView configBundle, JSONView config) {
        Set<String> categoriesBundleBlacklist = configGetStringSet(configBundle, "categoriesBlacklist", false);
        Set<String> categoriesBlacklist = configGetStringSet(config, "categoriesBlacklist", false);

        if (categoriesBlacklist == null & categoriesBundleBlacklist == null) return null;
        if (categoriesBlacklist == null) return categoriesBundleBlacklist;
        if (categoriesBundleBlacklist == null) return categoriesBlacklist;
        categoriesBlacklist.addAll(categoriesBundleBlacklist);
        return categoriesBlacklist;
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


    private TextFormatting dbBuildTextFormatting(String featureName, String sofaFile, String sofaID, Collection<String> categoriesWhitelist, Collection<String> categoriesBlacklist, Map<String, Color> categoryColorMap, String generatorID, String text, String style) throws SQLException {
        ArrayList<TextFormatting.Dataset.Segment> segments = dbCreateTextFormattingSegments(featureName, sofaFile, sofaID, categoriesWhitelist, categoriesBlacklist);
        TextFormatting.Dataset ds = new TextFormatting.Dataset(featureName, style, categoryColorMap, segments);
        return new TextFormatting(generatorID, sofaFile, sofaID, text, new ArrayList<>(List.of(ds)));
    }

    private ArrayList<TextFormatting.Dataset.Segment> dbCreateTextFormattingSegments (String featureName, String sofaFile, String sofaID, Collection<String> categoriesWhitelist, Collection<String> categoriesBlacklist) throws SQLException {
        ArrayList<TextFormatting.Dataset.Segment> segments = new ArrayList<>();

        try (Connection connection = dbAccess.getDataSource().getConnection()) {
            DSLContext dsl = DSL.using(connection);
            QueryHelper q = new QueryHelper(dsl);

            Table<?> table            = q.table(annotationTypeName);
            Field<Object> category    = q.field(annotationTypeName, featureName);
            Field<Object> begin       = q.field(annotationTypeName, featureNames.get("begin"));
            Field<Object> end         = q.field(annotationTypeName, featureNames.get("end"));
            Field<Object> filename    = q.field(annotationTypeName, "filename");
            Field<Object> sofa        = q.field(annotationTypeName, featureNames.get("sofa"));

            SelectConditionStep<? extends Record> query = q.dsl()
                    .select(category, begin, end)
                    .from(table)
                    .where(filename.equalIgnoreCase(sofaFile).and(sofa.eq(sofaID)));
            if (categoriesWhitelist != null) query = query.and(category.in(categoriesWhitelist));
            if (categoriesBlacklist != null) query = query.and(category.notIn(categoriesBlacklist));
            Result<? extends Record> result = query.fetch();

            for (Record record : result) {
                int substringBegin = record.get(begin, Integer.class);
                int substringEnd = record.get(end, Integer.class);
                String substringCategory = record.get(category, String.class);

                segments.add(new TextFormatting.Dataset.Segment(substringBegin, substringEnd, substringCategory));
            }
        }

        return segments;
    }

    private Map<String, Color> dbCreateCategoryColorMap(String featureName, Collection<String> sourceFiles, Collection<String> categoriesWhitelist, Collection<String> categoriesBlacklist) throws SQLException {
        return dbCreateCategoryColorMap(featureName, sourceFiles, categoriesWhitelist, categoriesBlacklist, null);
    }

    private Map<String, Color> dbCreateCategoryColorMap(String featureName, Collection<String> sourceFiles, Collection<String> categoriesWhitelist, Collection<String> categoriesBlacklist, Color singleColor) throws SQLException {
        Map<String, Map<String, Double>> categoryCountMap = dbCreateCategoryCountMap(featureName, sourceFiles, categoriesWhitelist, categoriesBlacklist);
        Map<String, Double> totalCategories = CategoryNumberMapping.calculateTotalFromCategoryCountMap(categoryCountMap);
        if (singleColor == null) return CategoryNumberColorMapping.categoryColorMapFromCategoriesNumberMap(totalCategories);
        return totalCategories.keySet().stream().collect(Collectors.toMap(k -> k, k -> singleColor));
    }

    private Map<String, Map<String, Double>> dbCreateCategoryCountMap(String featureName, Collection<String> sourceFiles, Collection<String> categoriesWhitelist, Collection<String> categoriesBlacklist) throws SQLException {
        if (sourceFiles == null || sourceFiles.isEmpty()) {
            if (sourceFiles != null) {
                System.out.println("Warning: Got empty source files list when trying to build a generator for feature \"" + featureName + "\". Defaulting to source files of Source object.");
            }
            sourceFiles = this.sourceFiles;
        }

        try (Connection connection = dbAccess.getDataSource().getConnection()) {
            DSLContext dsl = DSL.using(connection);
            QueryHelper q = new QueryHelper(dsl);

            Table<?> table = q.table(annotationTypeName);
            Field<Object> category = q.field(annotationTypeName, featureName);
            Field<Object> filename = q.field(annotationTypeName, "filename");
            Field<Integer> count = DSL.count();

            SelectConditionStep<? extends Record> query = q.dsl()
                    .select(filename, category, count)
                    .from(table)
                    .where(filename.in(sourceFiles));
            if (categoriesWhitelist != null) query = query.and(category.in(categoriesWhitelist));
            if (categoriesBlacklist != null) query = query.and(category.notIn(categoriesBlacklist));
            Result<? extends Record> result = query.groupBy(filename, category).fetch();

            HashMap<String, Map<String, Double>> fileCategoryCountMapping = new HashMap<>();

            result.forEach(record -> {
                String file = record.getValue(filename).toString();
                String cat = record.getValue(category).toString();
                Double number = record.getValue(count).doubleValue();

                fileCategoryCountMapping
                        .computeIfAbsent(file, k -> new HashMap<>())
                        .put(cat, number);
            });

            return fileCategoryCountMapping;
        }
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
        try (Connection connection = dbAccess.getDataSource().getConnection()) {
            DSLContext dsl = DSL.using(connection);
            QueryHelper q = new QueryHelper(dsl);

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

    private Set<String> configGetStringSet(JSONView config, String key, boolean returnEmptyIfNotConfigured) {
        try {
            JSONView sourceFiles = config.get("settings").get(key);
            if (sourceFiles.isList()) {
                List<?> list = sourceFiles.asList();
                boolean allStrings = list.stream().allMatch(item -> item instanceof String);
                if (allStrings) {
                    @SuppressWarnings("unchecked")
                    List<String> stringList = (List<String>) list;
                    return new HashSet<>(stringList);
                }
            }
            if (returnEmptyIfNotConfigured) {
                return new HashSet<>();
            }
            return null;
        } catch (Exception ignored) {
            if (returnEmptyIfNotConfigured) {
                return new HashSet<>();
            }
            return null;
        }
    }
    private Collection<String> configGetSourceFiles(JSONView config, String key) {
        return configGetStringSet(config, key, true);
    }

    private Set<String> dbGetAllSourceFiles() throws SQLException {
        return dbAccess.getSourceFiles();
    }
}
