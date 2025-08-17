package uni.textimager.sandbox.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import uni.textimager.sandbox.api.DataQueryHandler;

import java.util.*;

@RestController
@RequestMapping("/api")
public class APIController {

    private final DataQueryHandler handler;

    public APIController(DataQueryHandler handler) {
        this.handler = handler;
    }

    @GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getData(@RequestParam Map<String, String> params) {
        String id = params.get("id");
        String type = params.get("type");
        if (id == null || id.isBlank() || type == null || type.isBlank()) {
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"error\":\"Missing required query params: id, type\"}");
        }
        boolean pretty = Optional.ofNullable(params.get("pretty"))
                .map(v -> v.equalsIgnoreCase("true") || v.equals("1"))
                .orElse(false);

        // All other params are filters; reserved keys removed
        Map<String, String> filters = new LinkedHashMap<>(params);
        filters.keySet().removeAll(Set.of("id", "type", "pretty", "attrs"));

        String json = handler.buildArrayJson(id, type,
                Optional.ofNullable(params.get("attrs")).orElse(""),
                filters, pretty);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(json);
    }
}
