package uni.textimager.sandbox.api.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.textimager.sandbox.api.dto.CreatePipelineRequest;
import uni.textimager.sandbox.api.dto.PipelineResponse;
import uni.textimager.sandbox.api.dto.UpdatePipelineRequest;
import uni.textimager.sandbox.api.service.PipelineService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/pipelines")
public class PipelineController {

    private final PipelineService service;

    public PipelineController(PipelineService service) {
        this.service = service;
    }

    // List names with optional search + pagination
    @GetMapping
    public ResponseEntity<List<String>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String q
    ) throws Exception {
        return ResponseEntity.ok(service.listNames(page, size, q));
    }

    // Get full JSON by name
    @GetMapping("/{id}")
    public ResponseEntity<PipelineResponse> get(@PathVariable String id) throws Exception {
        JsonNode json = service.get(id);
        return ResponseEntity.ok(new PipelineResponse(id, json));
    }

    // Create new pipeline
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreatePipelineRequest req) throws Exception {
        service.create(req.name(), req.json());
        return ResponseEntity.created(
                // location header: /api/pipelines/{name}
                java.net.URI.create("/api/pipelines/" + req.name())
        ).build();
    }

    // Update/replace JSON of an existing pipeline
    @PutMapping("/{name}")
    public ResponseEntity<Void> update(@PathVariable String name,
                                       @Valid @RequestBody UpdatePipelineRequest req) throws Exception {
        service.update(name, req.json());
        return ResponseEntity.noContent().build();
    }

    // Delete by name
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable String name) throws Exception {
        service.delete(name);
        return ResponseEntity.noContent().build();
    }
}
