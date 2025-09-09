package uni.textimager.sandbox.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

	@PostMapping("/submit")
	public String submit(@RequestParam("file") MultipartFile file, Model model) throws Exception {
		String configs = new String(file.getBytes(), StandardCharsets.UTF_8);

		model.addAttribute("title", "Dynamic Visualizations");
		model.addAttribute("configs", configs);

		return "index";
	}

	@GetMapping("/pipeline")
	public String index(@RequestParam("id") String id, Model model) throws Exception {
		URI uri = URI.create("http://localhost:8080/api/visualisations?pipelineId=" + id);
		HttpRequest request = HttpRequest.newBuilder(uri).build();

		HttpResponse<String> response = this.client.send(request, this.bodyHandler);

		String configs = response.body();

		// String configs =
		// Files.readString(Paths.get("./src/main/resources/pipelines/examples/configs.json"));
		// String filters =
		// Files.readString(Paths.get("./src/main/resources/pipelines/examples/filters.json"));

		model.addAttribute("title", "Dynamic Visualizations");
		// model.addAttribute("filters", filters);
		model.addAttribute("configs", configs);

		return "index";
	}
}
