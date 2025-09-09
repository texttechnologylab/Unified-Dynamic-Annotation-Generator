// uni.textimager.sandbox.api.Controller.DataController

package uni.textimager.sandbox.api.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uni.textimager.sandbox.api.Handler.DataQueryHandler;
import uni.textimager.sandbox.api.Repositories.VisualisationsRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataQueryHandler handler;
    private final VisualisationsRepository visRepo;

    public DataController(DataQueryHandler handler, VisualisationsRepository visRepo) {
        this.handler = handler;
        this.visRepo = visRepo;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getData(@RequestParam Map<String, String> params) {
        String visId = params.get("id");
        if (visId == null || visId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"id is required\"}");
        }

        String pipelineId = Optional.ofNullable(params.get("pipelineId")).orElse("main");
        boolean pretty = Boolean.parseBoolean(Optional.ofNullable(params.get("pretty")).orElse("false"));
        String attrsCsv = Optional.ofNullable(params.get("attrs")).orElse("");

        // Build filters = all params minus reserved
        Map<String, String> filters = new LinkedHashMap<>(params);
        filters.keySet().removeAll(Set.of("id", "pipelineId", "pretty", "attrs"));

        // Resolve generatorId + chart type from VISUALIZATIONJSONS JSON
        var meta = visRepo.findMeta(pipelineId, visId).or(() -> visRepo.findMeta(visId));
        if (meta.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"visualization not found\",\"id\":\"" + visId + "\"}");
        }

        String generatorId = meta.get().generatorId();
        String chartType = meta.get().type();

        String json = handler.buildArrayJson(generatorId, chartType, attrsCsv, filters, pretty);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(json);
    }
}
