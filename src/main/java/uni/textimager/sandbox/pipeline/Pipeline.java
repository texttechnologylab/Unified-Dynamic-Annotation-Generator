package uni.textimager.sandbox.pipeline;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import uni.textimager.sandbox.generators.Generator;
import uni.textimager.sandbox.sources.DBAccess;
import uni.textimager.sandbox.sources.Source;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

@Getter
public class Pipeline {

    private final Map<String, PipelineNode> visualizations;
    private final Map<String, PipelineNode> generators;
    private final Map<String, PipelineNode> sources;
    private final List<PipelineNode> customTypes;
    private final Map<String, PipelineNode> filteredGenerators;
    private final Map<String, PipelineNode> filteredSources;

    private final String name;
    private final JSONView rootJSONView;


    public static Pipeline fromJSON(String path) throws IOException {
        ArrayList<?> pipelines = generateMapFromJSON(path);

        if (pipelines.size() != 1) {
            String append;
            if (pipelines.size() > 1) {
                append = "Multiple pipelines defined. If you want to read multiple pipelines, use function Pipeline.multipleFromJSON().";
            } else {
                append = "No pipelines defined.";
            }
            throw new IllegalArgumentException("Invalid pipeline JSON: " + append);
        }

        if (!(pipelines.get(0) instanceof Map<?, ?> pipeline)) {
            throw new IllegalArgumentException("Invalid pipeline JSON.");
        }

        JSONView view = new JSONView(pipeline);
        return generatePipelineFromJSONView(view);
    }

    public static ArrayList<Pipeline> multipleFromJSON(String path) {
        // TODO: Implement, using generatePipelineFromJSONView
        return null;
    }


    public List<Generator> generateGenerators(DBAccess dbAccess) throws SQLException {
        return generateGenerators(dbAccess, true, true);
    }
    public List<Generator> generateGenerators(DBAccess dbAccess, boolean onlyRelevantSources, boolean onlyRelevantGenerators) throws SQLException {
        Map<String, PipelineNode> sourceNodes = onlyRelevantSources ? filteredSources : sources;
        Map<String, PipelineNode> generatorNodes = onlyRelevantGenerators ? filteredGenerators : generators;
        ArrayList<Generator> generatedGenerators = new ArrayList<>();
        for (PipelineNode sourceNode : sourceNodes.values()) {
            Source s = new Source(dbAccess, sourceNode.getConfig(), generatorNodes, sourceNode.getChildren());
            System.out.println("Source created: " + s.getConfig().get("name"));
            List<Generator> sourceGenerators = s.createGenerators();
            generatedGenerators.addAll(sourceGenerators);
            System.out.println(sourceGenerators.size() + " Generators created for source " + s.getName());
        }
        return generatedGenerators;
    }


    private Pipeline(String name, Map<String, PipelineNode> visualizations, Map<String, PipelineNode> generators, Map<String, PipelineNode> sources, List<PipelineNode> customTypes, JSONView rootJSONView) {
        this.name = name;
        this.visualizations = visualizations;
        this.generators = generators;
        this.sources = sources;
        this.customTypes = customTypes;
        this.rootJSONView = rootJSONView;

        System.out.println("Filtering irrelevant pipeline nodes...");
        HashMap<String, PipelineNode> filteredSources = new HashMap<>();
        HashMap<String, PipelineNode> filteredGenerators = new HashMap<>();
        for (PipelineNode v : visualizations.values()) {
            filterPipeline(v, filteredSources, filteredGenerators);
        }
        System.out.println("Non-relevant Sources:");
        System.out.println(Arrays.toString(keysOnlyInA(sources, filteredSources)));
        System.out.println("Non-relevant Generators:");
        System.out.println(Arrays.toString(keysOnlyInA(generators, filteredGenerators)));

        this.filteredGenerators = filteredGenerators;
        this.filteredSources = filteredSources;
    }

    private static ArrayList<?> generateMapFromJSON(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = Pipeline.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("File not found: " + path);
            }
            Map<String, Object> data = mapper.readValue(in, new TypeReference<>() {});
            if (!(data.get("pipelines") instanceof ArrayList<?> pipelines)) {
                throw new IllegalArgumentException("Invalid pipeline JSON.");
            }
            return pipelines;
        }
    }

    private static Pipeline generatePipelineFromJSONView(JSONView pipelineView) throws IllegalArgumentException {
        try {
            // Step 1: Generate all pipeline nodes from JSON
            String name = pipelineView.get("name").toString();
            JSONView sourcesView = pipelineView.get("sources");
            HashMap<String, PipelineNode> sources = new HashMap<>();
            HashMap<String, PipelineNode> generators = new HashMap<>();
            for (JSONView sourcesEntry : sourcesView) {
                PipelineNode current = new PipelineNode(PipelineNodeType.SOURCE, new HashMap<>(), sourcesEntry);
                String sourceName = sourcesEntry.get("name").toString();
                sources.put(sourceName, current);
                JSONView createsGenerators = sourcesEntry.get("createsGenerators");
                for (JSONView generatorEntry : createsGenerators) {
                    if (generatorEntry.get("type").toString().equals("combi")) {
                        JSONView createsSubGenerators = generatorEntry.get("createsGenerators");
                        for (JSONView subGeneratorEntry : createsSubGenerators) {
                            HashMap<String, PipelineNode> generatorDependencies = new HashMap<>();
                            generatorDependencies.put(sourceName, current);
                            PipelineNode generator = new PipelineNode(PipelineNodeType.GENERATOR, generatorDependencies, subGeneratorEntry);
                            generators.put(subGeneratorEntry.get("name").toString(), generator);
                        }
                    } else {
                        HashMap<String, PipelineNode> generatorDependencies = new HashMap<>();
                        generatorDependencies.put(sourceName, current);
                        PipelineNode generator = new PipelineNode(PipelineNodeType.GENERATOR, generatorDependencies, generatorEntry);
                        generators.put(generatorEntry.get("name").toString(), generator);
                    }
                }
            }
            JSONView visualizationsView = pipelineView.get("visualizations");
            HashMap<String, PipelineNode> visualizations = (HashMap<String, PipelineNode>) generatePipelineVisualizationsFromJSONView(visualizationsView, generators);

            // Step 2: Generate customTypes if defined
            List<PipelineNode> customTypes = generatePipelineCustomTypesFromJSONView(pipelineView);

            return new Pipeline(name, visualizations, generators, sources, customTypes, pipelineView);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid pipeline JSON.");
        }
    }

    private static List<PipelineNode> generatePipelineCustomTypesFromJSONView(JSONView pipelineView) {
        ArrayList<PipelineNode> customTypes = new ArrayList<>();
        try {
            JSONView customTypesView = pipelineView.get("customTypes");
            for (JSONView customTypeEntry : customTypesView) {
                // TODO: integrate customTypes into pipeline with dependencies and filter non-used types. Make it a map like the other PipelineNode collections.
                PipelineNode current = new PipelineNode(PipelineNodeType.CUSTOMTYPE, new HashMap<>(), customTypeEntry);
                customTypes.add(current);
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return customTypes;
    }

    private static Map<String, PipelineNode> generatePipelineVisualizationsFromJSONView(JSONView visualizationsView, Map<String, PipelineNode> generators) {
        if (!visualizationsView.isList()) {
            throw new IllegalArgumentException("Invalid pipeline JSON: \"visualizations\" must be a list.");
        }
        HashMap<String, PipelineNode> visualizations = new HashMap<>();
        try {
            for (JSONView visualizationEntry : visualizationsView) {
                HashMap<String, PipelineNode> dependencies;
                if (visualizationEntry.get("type").toString().equals("combi")) {
                    dependencies = (HashMap<String, PipelineNode>) generatePipelineVisualizationsFromJSONView(visualizationEntry.get("visualizations"), generators);
                    visualizations.putAll(dependencies);
                } else {
                    dependencies = new HashMap<>();
                    String generatorName = visualizationEntry.get("generator").get("name").toString();
                    dependencies.put(generatorName, generators.get(generatorName));
                }
                PipelineNode current = new PipelineNode(PipelineNodeType.VISUALIZATION, dependencies, visualizationEntry);
                visualizations.put(visualizationEntry.get("name").toString(), current);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid pipeline JSON.");
        }
        return visualizations;
    }

    private static void filterPipeline(PipelineNode current, Map<String, PipelineNode> filteredSources, Map<String, PipelineNode> filteredGenerators) {
        if (current.getType() == PipelineNodeType.SOURCE) {
            filteredSources.put(current.getConfig().get("name").toString(), current);
        } else if (current.getType() == PipelineNodeType.GENERATOR) {
            filteredGenerators.put(current.getConfig().get("name").toString(), current);
        }
        for (PipelineNode dependency : current.getDependencies().values()) {
            filterPipeline(dependency, filteredSources, filteredGenerators);
        }
    }

    private static String[] keysOnlyInA(Map<String, ?> mapA, Map<String, ?> mapB) {
        List<String> uniqueKeys = new ArrayList<>();
        for (String key : mapA.keySet()) {
            if (!mapB.containsKey(key)) {
                uniqueKeys.add(key);
            }
        }
        return uniqueKeys.toArray(new String[0]);
    }
}
