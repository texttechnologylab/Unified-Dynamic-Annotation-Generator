package uni.textimager.sandbox.api;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DummyDataProvider {

    // For demo, ignore id/type and return the static array you provided.
    public List<Map<String, Object>> getData(String id, String type) {
        return List.of(
                Map.of("label", "NN", "value", 140.0, "color", "#ff0000"),
                Map.of("label", "KOKOM", "value", 2.0, "color", "#b24363"),
                Map.of("label", "PDAT", "value", 6.0, "color", "#9acd32"),
                Map.of("label", "ART", "value", 73.0, "color", "#0000ff"),
                Map.of("label", "ADJD", "value", 24.0, "color", "#000080"),
                Map.of("label", "ADJA", "value", 39.0, "color", "#ffff00"),
                Map.of("label", "$(", "value", 6.0, "color", "#15c07d"),
                Map.of("label", "PTKVZ", "value", 4.0, "color", "#3d51de"),
                Map.of("label", "PRF", "value", 2.0, "color", "#88934b"),
                Map.of("label", "PPOSAT", "value", 9.0, "color", "#00ff7f"),
                Map.of("label", "PRELS", "value", 5.0, "color", "#d39b55"),
                Map.of("label", "FM", "value", 2.0, "color", "#946969"),
                Map.of("label", "$,", "value", 46.0, "color", "#ff00ff"),
                Map.of("label", "PPER", "value", 40.0, "color", "#00ffff"),
                Map.of("label", "PIAT", "value", 7.0, "color", "#00bfff"),
                Map.of("label", "$.", "value", 56.0, "color", "#00ff00"),
                Map.of("label", "VMINF", "value", 2.0, "color", "#8cf397"),
                Map.of("label", "VMFIN", "value", 5.0, "color", "#843652"),
                Map.of("label", "PTKZU", "value", 5.0, "color", "#a2b729"),
                Map.of("label", "VVFIN", "value", 32.0, "color", "#008080"),
                Map.of("label", "PROAV", "value", 14.0, "color", "#ff69b4"),
                Map.of("label", "PTKNEG", "value", 5.0, "color", "#629977"),
                Map.of("label", "CARD", "value", 4.0, "color", "#b1600b"),
                Map.of("label", "ADV", "value", 34.0, "color", "#808080"),
                Map.of("label", "PWAV", "value", 6.0, "color", "#63c62a"),
                Map.of("label", "PDS", "value", 1.0, "color", "#01769f"),
                Map.of("label", "VVIZU", "value", 3.0, "color", "#760a06"),
                Map.of("label", "KOUS", "value", 9.0, "color", "#ffa500"),
                Map.of("label", "VVINF", "value", 11.0, "color", "#8b4513"),
                Map.of("label", "PTKANT", "value", 1.0, "color", "#9e0bcb"),
                Map.of("label", "VVPP", "value", 4.0, "color", "#5ae18a"),
                Map.of("label", "KON", "value", 35.0, "color", "#ffafaf"),
                Map.of("label", "VAINF", "value", 3.0, "color", "#3a87c5"),
                Map.of("label", "APPR", "value", 45.0, "color", "#ffc800"),
                Map.of("label", "APPRART", "value", 6.0, "color", "#182121"),
                Map.of("label", "NE", "value", 26.0, "color", "#800080"),
                Map.of("label", "APZR", "value", 1.0, "color", "#417d62"),
                Map.of("label", "VAFIN", "value", 26.0, "color", "#808000"),
                Map.of("label", "PIS", "value", 3.0, "color", "#7c5e64"));
    }
}
