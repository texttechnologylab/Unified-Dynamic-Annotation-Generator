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
		model.addAttribute("pipelines", "[\"main\", \"example_pipeline\", \"pipeline2\"]");

		return "/pages/index/index";
	}

	@GetMapping("/editor")
	public String editorNew(Model model) throws Exception {
		String config = "{}";

		model.addAttribute("config", config);

		return "/pages/editor/editor";
	}

	@PostMapping("/editor")
	public String editorFile(@RequestParam("file") MultipartFile file, Model model) throws Exception {
		String config = new String(file.getBytes(), StandardCharsets.UTF_8);

		model.addAttribute("config", config);

		return "/pages/editor/editor";
	}

	@GetMapping("/editor/{id}")
	public String editorEdit(@PathVariable("id") String id, Model model) throws Exception {
		String config = "{\"widgets\": " + this.fetch("http://localhost:8080/api/visualisations?pipelineId=" + id)
				+ "}";

		model.addAttribute("config", config);

		return "/pages/editor/editor";
	}

	@GetMapping("/view/{id}")
	public String view(@PathVariable("id") String id, Model model) throws Exception {
		String widgets = this.fetch("http://localhost:8080/api/visualisations?pipelineId=" + id);

		model.addAttribute("id", id);
		model.addAttribute("pipelines", "[\"main\", \"example_pipeline\", \"pipeline2\"]");
		model.addAttribute("widgets", widgets);

		return "/pages/view/view";
	}
}
