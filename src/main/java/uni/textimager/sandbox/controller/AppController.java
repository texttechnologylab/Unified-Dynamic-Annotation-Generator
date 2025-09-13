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

	public String fetch(String url) throws Exception {
		URI uri = URI.create(url);
		HttpRequest request = HttpRequest.newBuilder(uri).build();
		HttpResponse<String> response = this.client.send(request, this.bodyHandler);

		return response.body();
	}

	@GetMapping("/")
	public String index(Model model) throws Exception {
		model.addAttribute("title", "Dynamic Visualizations");
		model.addAttribute("pipelines", "[\"main\", \"example_pipeline\", \"pipeline2\"]");

		return "index";
	}

	@GetMapping("/editor")
	public String editorNew(Model model) throws Exception {
		String json = "";

		model.addAttribute("title", "Editor - Dynamic Visualizations");
		model.addAttribute("json", json);

		return "editor";
	}

	@PostMapping("/editor")
	public String editorFile(@RequestParam("file") MultipartFile file, Model model) throws Exception {
		String json = new String(file.getBytes(), StandardCharsets.UTF_8);

		model.addAttribute("title", "Editor - Dynamic Visualizations");
		model.addAttribute("json", json);

		return "editor";
	}

	@GetMapping("/editor/{id}")
	public String editorEdit(@PathVariable("id") String id, Model model) throws Exception {
		String json = this.fetch("http://localhost:8080/api/visualisations?pipelineId=" + id);

		model.addAttribute("title", id + " - Editor - Dynamic Visualizations");
		model.addAttribute("json", json);

		return "editor";
	}

	@GetMapping("/pipeline/{id}")
	public String pipeline(@PathVariable("id") String id, Model model) throws Exception {
		String configs = this.fetch("http://localhost:8080/api/visualisations?pipelineId=" + id);
		String filters = Files.readString(Paths.get("./src/main/resources/pipelines/examples/filters.json"));

		model.addAttribute("pipeline", id);
		model.addAttribute("filters", filters);
		model.addAttribute("configs", configs);

		return "pipeline";
	}
}
