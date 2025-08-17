package uni.textimager.sandbox.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class AppController {
	ObjectMapper mapper = new ObjectMapper();

	@GetMapping("/examples")
	public String examples(Model model) throws JsonProcessingException {
		model.addAttribute("title", "Examples");

		return "examples";
	}

	@GetMapping("/")
	public String index(Model model) throws IOException {
		String configs = Files.readString(Paths.get("./src/main/resources/static/data/configs.json"));

		model.addAttribute("title", "Dynamic Visualizations");
		model.addAttribute("configs", configs);

		return "index";
	}
}
