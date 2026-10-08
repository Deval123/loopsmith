package loopsmith.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;
import loopsmith.LoopsmithProperties;
import loopsmith.core.GpxWriter;
import loopsmith.core.IntentParser;
import loopsmith.core.RouteIntent;
import loopsmith.core.RouteShape;
import loopsmith.routing.RouteResult;
import loopsmith.routing.RoutingClient;
import loopsmith.routing.RoutingException;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    /** A route within 10% of the requested distance is good enough. */
    static final double TOLERANCE = 0.10;

    /** Round-trip routing is approximate; never ask the engine for more than 1.5x or less than 1/1.5x. */
    static final double MAX_CORRECTION = 1.5;

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
        double target = intent.distanceMeters();

        List<RouteResult> pool = new ArrayList<>(candidates(lat, lon, intent, target));
        RouteResult closest = closest(pool, target)
                .orElseThrow(() -> new RoutingException("No candidate route could be built"));

        // The engine treats the distance as a hint and usually undershoots. When no loop is within the
        // tolerance, ask again with a corrected distance and add those loops to the pool.
        if (!withinTolerance(closest, target) && closest.distanceMeters() > 0) {
            double factor = Math.max(1 / MAX_CORRECTION, Math.min(MAX_CORRECTION, target / closest.distanceMeters()));
            pool.addAll(candidates(lat, lon, intent, target * factor));
        }

        RouteResult best = choose(pool, target);

        String name = String.format(Locale.ROOT, "%s loop, %.1f km", intent.activity().name().toLowerCase(Locale.ROOT),
                best.distanceMeters() / 1000.0);
        return new RouteOutcome(name, best.distanceMeters() / 1000.0, best.ascendMeters(),
                GpxWriter.write(name, best.points()));
    }

    /**
     * Among the loops that are close enough to the requested distance, prefer the roundest one (it retraces
     * the fewest streets). If none is close enough, fall back to the one closest in length.
     */
    private static RouteResult choose(Collection<RouteResult> pool, double target) {
        return pool.stream()
                .filter(r -> withinTolerance(r, target))
                .max(Comparator.comparingDouble(r -> RouteShape.roundness(r.points())))
                .or(() -> closest(pool, target))
                .orElseThrow(() -> new RoutingException("No candidate route could be built"));
    }

    private static boolean withinTolerance(RouteResult route, double target) {
        return Math.abs(route.distanceMeters() - target) / target <= TOLERANCE;
    }

    /** Several seeds give several different loops. */
    private List<RouteResult> candidates(double lat, double lon, RouteIntent intent, double distanceMeters) {
        return IntStream.rangeClosed(1, candidateSeeds)
                .mapToObj(seed -> tryRoute(lat, lon, intent, distanceMeters, seed))
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<RouteResult> closest(Collection<RouteResult> routes, double target) {
        return routes.stream().min(Comparator.comparingDouble(r -> Math.abs(r.distanceMeters() - target)));
    }

    private Optional<RouteResult> tryRoute(double lat, double lon, RouteIntent intent, double distanceMeters, int seed) {
        try {
            return Optional.of(routing.roundTrip(lat, lon, distanceMeters, intent.activity(), seed));
        } catch (RoutingException e) {
            return Optional.empty();
        }
    }
}
