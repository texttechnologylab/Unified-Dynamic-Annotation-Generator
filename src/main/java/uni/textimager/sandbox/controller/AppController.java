package uni.textimager.sandbox.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class AppController {
	HttpClient client = HttpClient.newHttpClient();
	BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();

	@GetMapping("/")
	public String index(Model model) throws Exception {
		model.addAttribute("title", "Dynamic Visualizations");

		return "index";
	}

	@GetMapping("/editor/{id}")
	public String editor(@PathVariable("id") String id, Model model) throws Exception {
		model.addAttribute("title", id + " - Editor - Dynamic Visualizations");

		return "editor";
	}

	@GetMapping("/pipeline/{id}")
	public String pipeline(@PathVariable("id") String id, Model model) throws Exception {
		URI uri = URI.create("http://localhost:8080/api/visualisations?pipelineId=" + id);
		HttpRequest request = HttpRequest.newBuilder(uri).build();

		HttpResponse<String> response = this.client.send(request, this.bodyHandler);

		String configs = response.body();
		String filters = Files.readString(Paths.get("./src/main/resources/pipelines/examples/filters.json"));

		model.addAttribute("pipeline", id);
		model.addAttribute("filters", filters);
		model.addAttribute("configs", configs);

		return "pipeline";
	}

	@PostMapping("/submit")
	public String submit(@RequestParam("file") MultipartFile file, Model model) throws Exception {
		String configs = new String(file.getBytes(), StandardCharsets.UTF_8);

		model.addAttribute("title", "Dynamic Visualizations");
		model.addAttribute("configs", configs);

		return "editor";
	}
}
