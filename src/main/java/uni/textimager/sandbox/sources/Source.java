package uni.textimager.sandbox.sources;

import lombok.Getter;
import uni.textimager.sandbox.generators.Generator;
import uni.textimager.sandbox.pipeline.JSONView;
import uni.textimager.sandbox.pipeline.PipelineNode;
import uni.textimager.sandbox.pipeline.PipelineNodeType;

import java.util.*;


@Getter
public class Source implements SourceInterface {

    public static final String DEFAULT_TYPE_POS = "POS";
    public static final String DEFAULT_TYPE_LEMMA = "Lemma";



    private final JSONView config;
    private final Collection<PipelineNode> relevantGenerators;
    private final String name;
    private final String type;
    private final String annotationTypeName;
    private final Map<String, String> featureNames;
    private final Collection<String> sourceFilesWhitelist;
    private final Collection<String> sourceFilesBlacklist;



    public Source(JSONView config, Collection<PipelineNode> relevantGenerators) {
        this.config = config;
        this.relevantGenerators = relevantGenerators;

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
        this.sourceFilesWhitelist = configGetSourceFiles("sourceFilesWhitelist");
        this.sourceFilesBlacklist = configGetSourceFiles("sourceFilesBlacklist");
    }

    // Don't leave out filtered generators that are part of a combi with at least one relevant generator to keep visualization results consistent
    @Override
    public <T extends Generator> Collection<T> createGenerators() {
        if (type.equals(DEFAULT_TYPE_POS)) {

        } else if (type.equals(DEFAULT_TYPE_LEMMA)) {

        } else {
            // Default case still supports some basic generators
        }
        return List.of();
    }

    // Recursive if wanted combi is not defined TODO: Print warning in that case
    private <T extends Generator> Collection<T> createGeneratorsFromPipelineNodes(Collection<PipelineNode> generators) {
        if (generators.isEmpty()) {
            System.out.println("Warning: empty generator collection for source: " + name);
            return List.of();
        }
        if (!containsOnlyGenerators(generators)) {
            throw new IllegalArgumentException("Source " + name + " contains non-generator nodes, returning empty collection.");
        }
        if (generators.size() == 1) {
            PipelineNode generator = generators.iterator().next();
            String generatorType = generator.getConfig().get(type).toString();
            switch (generatorType) {
                case "CategoryNumberMapping", "CategoryNumberColorMapping":
                    break;
                case "Lemma":
                    break;
                default:
                    throw new IllegalArgumentException("Unknown generator type: " + generator.getConfig().get(type) + " for source: " + name);
            }
        }
        return List.of();
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


    public static void main(String[] args) {
        // Source s = new Source();
        // CategoryNumberMapping m = s.createGenerator(CategoryNumberMapping.class);
    }
}
