package loopsmith.api;

import java.util.Map;
import loopsmith.routing.RoutingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    static final int MAX_PROMPT_CHARS = 300;

    private static final Logger log = LoggerFactory.getLogger(RouteController.class);

    private final RouteService service;

    public RouteController(RouteService service) {
        this.service = service;
    }

    public record RouteRequest(String prompt, double lat, double lon) {
    }

    @PostMapping
    public RouteOutcome create(@RequestBody RouteRequest request) {
        validate(request);
        return service.build(request.prompt().strip(), request.lat(), request.lon());
    }

    static void validate(RouteRequest r) {
        if (r == null || r.prompt() == null || r.prompt().isBlank()) {
            throw new IllegalArgumentException("prompt is required");
        }
        if (r.prompt().length() > MAX_PROMPT_CHARS) {
            throw new IllegalArgumentException("prompt is limited to " + MAX_PROMPT_CHARS + " characters");
        }
        if (!Double.isFinite(r.lat()) || r.lat() < -90 || r.lat() > 90
                || !Double.isFinite(r.lon()) || r.lon() < -180 || r.lon() > 180) {
            throw new IllegalArgumentException("lat/lon out of range");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RoutingException.class)
    ResponseEntity<Map<String, String>> routingFailed(RoutingException e) {
        // Coordinates and prompt are intentionally absent from the logs.
        log.warn("Routing failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "The routing engine is unavailable or found no route here"));
    }
}
