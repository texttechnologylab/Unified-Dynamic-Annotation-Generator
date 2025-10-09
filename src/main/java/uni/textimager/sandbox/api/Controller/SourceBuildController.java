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
        service.startBuild(pipeline, pipeline);
        return ResponseEntity.accepted().body("Build started");
    }
}
