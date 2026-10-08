package loopsmith.api;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;
import loopsmith.LoopsmithProperties;
import loopsmith.core.GpxWriter;
import loopsmith.core.IntentParser;
import loopsmith.core.RouteIntent;
import loopsmith.routing.RouteResult;
import loopsmith.routing.RoutingClient;
import loopsmith.routing.RoutingException;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private final IntentParser parser;
    private final RoutingClient routing;
    private final int candidateSeeds;

    public RouteService(IntentParser parser, RoutingClient routing, LoopsmithProperties props) {
        this.parser = parser;
        this.routing = routing;
        this.candidateSeeds = Math.max(1, props.graphhopper().candidateSeeds());
    }

    public RouteOutcome build(String prompt, double lat, double lon) {
        RouteIntent intent = parser.parse(prompt);

        // Several seeds give several different loops; keep the one closest to the requested distance.
        Optional<RouteResult> best = IntStream.rangeClosed(1, candidateSeeds)
                .mapToObj(seed -> tryRoute(lat, lon, intent, seed))
                .flatMap(Optional::stream)
                .min(Comparator.comparingDouble(r -> Math.abs(r.distanceMeters() - intent.distanceMeters())));

        RouteResult route = best.orElseThrow(() -> new RoutingException("No candidate route could be built"));

        String name = String.format(Locale.ROOT, "%s loop, %.1f km", intent.activity().name().toLowerCase(Locale.ROOT),
                route.distanceMeters() / 1000.0);
        return new RouteOutcome(name, route.distanceMeters() / 1000.0, route.ascendMeters(),
                GpxWriter.write(name, route.points()));
    }

    private Optional<RouteResult> tryRoute(double lat, double lon, RouteIntent intent, int seed) {
        try {
            return Optional.of(routing.roundTrip(lat, lon, intent.distanceMeters(), intent.activity(), seed));
        } catch (RoutingException e) {
            return Optional.empty();
        }
    }
}
