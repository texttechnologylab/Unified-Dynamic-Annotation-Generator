package uni.textimager.sandbox.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class AppController {
    ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/demo")
    public String demo(Model model) throws JsonProcessingException {
        List<Map<String, Object>> categories = List.of(
                Map.of("label", "Apples", "value", 30, "color", "#4269d0"),
                Map.of("label", "Bananas", "value", 70, "color", "#efb118"),
                Map.of("label", "Cherries", "value", 45, "color", "#ff725c"),
                Map.of("label", "Dates", "value", 65, "color", "#6cc5b0"),
                Map.of("label", "Elderberries", "value", 20, "color", "#3ca951"));

        List<Map<String, Object>> coordinates = List.of(
                Map.of("x", 0, "y", 5),
                Map.of("x", 20, "y", 20),
                Map.of("x", 40, "y", 10),
                Map.of("x", 60, "y", 40),
                Map.of("x", 80, "y", 5),
                Map.of("x", 100, "y", 60));

        model.addAttribute("title", "Demo");
        model.addAttribute("categories", this.mapper.writeValueAsString(categories));
        model.addAttribute("coordinates", this.mapper.writeValueAsString(coordinates));
        model.addAttribute("network",
                "{\"nodes\":[{\"id\":1,\"name\":\"A\",\"color\":\"#69b3a2\"},{\"id\":2,\"name\":\"B\",\"color\":\"#69b3a2\"},{\"id\":3,\"name\":\"C\",\"color\":\"#69b3a2\"},{\"id\":4,\"name\":\"D\",\"color\":\"#69b3a2\"},{\"id\":5,\"name\":\"E\",\"color\":\"#69b3a2\"},{\"id\":6,\"name\":\"F\",\"color\":\"#69b3a2\"},{\"id\":7,\"name\":\"G\",\"color\":\"#69b3a2\"},{\"id\":8,\"name\":\"H\",\"color\":\"#69b3a2\"},{\"id\":9,\"name\":\"I\",\"color\":\"#69b3a2\"},{\"id\":10,\"name\":\"J\",\"color\":\"#69b3a2\"}],\"links\":[{\"source\":1,\"target\":2},{\"source\":1,\"target\":5},{\"source\":1,\"target\":6},{\"source\":2,\"target\":3},{\"source\":2,\"target\":7},{\"source\":3,\"target\":4},{\"source\":8,\"target\":3},{\"source\":4,\"target\":5},{\"source\":4,\"target\":9},{\"source\":5,\"target\":10}]}");
        model.addAttribute("features",
                "[{type: \"LineString\", label: \"Flug 1\", coordinates: [[100, 60], [-60, -30]]}, {type: \"LineString\", label: \"Flug 2\", coordinates: [[10, -20], [-60, -30]]}, {type: \"LineString\", label: \"Flug 3\", coordinates: [[10, -20], [130, -30]]}, {type: \"Point\", label: \"Timbuktu\", coordinates: [-3.0026, 16.7666]}]");
        return "demo";
    }
}
