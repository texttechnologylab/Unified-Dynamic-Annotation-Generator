package uni.textimager.sandbox.api.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.textimager.sandbox.api.service.SourceBuildService;

import java.util.Map;

@RestController
@RequestMapping("/api/source-build")
@RequiredArgsConstructor
public class SourceBuildController {
    private final SourceBuildService service;

    @PostMapping
    public ResponseEntity<?> trigger(@RequestParam(required = false) String pipeline) {
        return service.startBuild(pipeline)
                ? ResponseEntity.accepted().body("Build started")
                : ResponseEntity.status(409).body("Already running");
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("running", service.isRunning());
    }
}
