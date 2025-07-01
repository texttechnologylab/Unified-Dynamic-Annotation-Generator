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

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Welcome");
        return "index";
    }

    @GetMapping("/charts")
    public String charts(Model model) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

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

        model.addAttribute("title", "Example Charts");
        model.addAttribute("categories", mapper.writeValueAsString(categories));
        model.addAttribute("coordinates", mapper.writeValueAsString(coordinates));
        return "charts";
    }
}
