package loopsmith.routing;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import loopsmith.LoopsmithProperties;
import loopsmith.core.Activity;
import loopsmith.core.TrackPoint;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Talks to a local GraphHopper instance (OpenStreetMap data) using its round-trip algorithm. */
@Component
public class GraphHopperClient implements RoutingClient {

    private final RestClient client;

    public GraphHopperClient(LoopsmithProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.graphhopper().timeoutMs());
        factory.setReadTimeout(props.graphhopper().timeoutMs());
        this.client = RestClient.builder()
                .baseUrl(props.graphhopper().baseUrl())
                .requestFactory(factory)
                .build();
    }

    @Override
    public RouteResult roundTrip(double lat, double lon, double distanceMeters, Activity activity, int seed) {
        // RUN and WALK both use the pedestrian profile for the MVP; see the roadmap for a dedicated run profile.
        String profile = "foot";
        String point = String.format(Locale.ROOT, "%.6f,%.6f", lat, lon);
        try {
            JsonNode body = client.get()
                    .uri(uri -> uri.path("/route")
                            .queryParam("point", point)
                            .queryParam("profile", profile)
                            .queryParam("algorithm", "round_trip")
                            .queryParam("round_trip.distance", Math.round(distanceMeters))
                            .queryParam("round_trip.seed", seed)
                            .queryParam("points_encoded", false)
                            .queryParam("elevation", false)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
            return parse(body);
        } catch (RestClientException e) {
            throw new RoutingException("GraphHopper request failed", e);
        }
    }

    static RouteResult parse(JsonNode body) {
        if (body == null || !body.path("paths").isArray() || body.path("paths").isEmpty()) {
            throw new RoutingException("GraphHopper returned no path");
        }
        JsonNode path = body.path("paths").get(0);
        JsonNode coords = path.path("points").path("coordinates");
        if (!coords.isArray() || coords.size() < 2) {
            throw new RoutingException("GraphHopper returned an empty track");
        }
        List<TrackPoint> points = new ArrayList<>(coords.size());
        for (JsonNode c : coords) {
            // GeoJSON order is [lon, lat(, ele)]
            Double ele = c.size() > 2 ? c.get(2).asDouble() : null;
            points.add(new TrackPoint(c.get(1).asDouble(), c.get(0).asDouble(), ele));
        }
        return new RouteResult(points, path.path("distance").asDouble(), path.path("ascend").asDouble(0));
    }
}
