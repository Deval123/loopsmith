package loopsmith.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import loopsmith.LoopsmithProperties;
import loopsmith.core.Activity;
import loopsmith.core.RuleBasedIntentParser;
import loopsmith.core.TrackPoint;
import loopsmith.routing.RouteResult;
import loopsmith.routing.RoutingClient;
import loopsmith.routing.RoutingException;
import org.junit.jupiter.api.Test;

class RouteServiceTest {

    private static final LoopsmithProperties PROPS =
            new LoopsmithProperties(null, new LoopsmithProperties.GraphHopper("http://unused", 1000, 3));

    /** Returns a loop whose length is {@code ratio} times the distance it was asked for. */
    private static final class ScaledRouting implements RoutingClient {
        final double ratio;
        int calls;

        ScaledRouting(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public RouteResult roundTrip(double lat, double lon, double distanceMeters, Activity activity, int seed) {
            calls++;
            return new RouteResult(
                    List.of(new TrackPoint(48.0, 2.0, null), new TrackPoint(48.001, 2.001, null)),
                    distanceMeters * ratio, 0);
        }
    }

    @Test
    void correctsAnEngineThatUndershoots() {
        ScaledRouting routing = new ScaledRouting(0.8);
        RouteService service = new RouteService(new RuleBasedIntentParser(), routing, PROPS);

        RouteOutcome outcome = service.build("a 6 km run", 48.0, 2.0);

        assertEquals(6.0, outcome.distanceKm(), 0.01);
        assertEquals(6, routing.calls, "3 seeds, then 3 more with the corrected distance");
    }

    @Test
    void doesNotRetryWhenWithinTolerance() {
        ScaledRouting routing = new ScaledRouting(0.95);
        RouteService service = new RouteService(new RuleBasedIntentParser(), routing, PROPS);

        service.build("a 6 km run", 48.0, 2.0);

        assertEquals(3, routing.calls);
    }

    @Test
    void neverAsksForMoreThanTheCorrectionCap() {
        ScaledRouting routing = new ScaledRouting(0.1);
        RouteService service = new RouteService(new RuleBasedIntentParser(), routing, PROPS);

        RouteOutcome outcome = service.build("a 10 km walk", 48.0, 2.0);

        // First pass: 1 km. Corrected request is capped at 1.5x = 15 km, giving 1.5 km, which is closer.
        assertTrue(outcome.distanceKm() <= 1.5 + 1e-9);
    }

    @Test
    void prefersTheRoundestLoopAmongThoseOfTheRightLength() {
        double lonPerLat = 1 / Math.cos(Math.toRadians(48.0));
        List<TrackPoint> thin = List.of(new TrackPoint(48.0, 2.0, null), new TrackPoint(48.019, 2.0, null),
                new TrackPoint(48.019, 2.0 + 0.001 * lonPerLat, null), new TrackPoint(48.0, 2.0 + 0.001 * lonPerLat, null));
        List<TrackPoint> square = List.of(new TrackPoint(48.0, 2.0, null), new TrackPoint(48.01, 2.0, null),
                new TrackPoint(48.01, 2.0 + 0.01 * lonPerLat, null), new TrackPoint(48.0, 2.0 + 0.01 * lonPerLat, null));

        // Seed 1 (thin) is the closest in length, seed 2 (square) is slightly off but still within tolerance.
        RoutingClient routing = (lat, lon, distance, activity, seed) ->
                seed == 1 ? new RouteResult(thin, distance, 0) : new RouteResult(square, distance * 0.95, 0);
        RouteService service = new RouteService(new RuleBasedIntentParser(), routing, PROPS);

        RouteOutcome outcome = service.build("a 6 km run", 48.0, 2.0);

        assertEquals(5.7, outcome.distanceKm(), 0.01, "the square loop must be chosen");
    }

    @Test
    void failsClearlyWhenNoRouteCanBeBuilt() {
        RoutingClient failing = (lat, lon, distance, activity, seed) -> {
            throw new RoutingException("boom");
        };
        RouteService service = new RouteService(new RuleBasedIntentParser(), failing, PROPS);

        assertThrows(RoutingException.class, () -> service.build("a 6 km run", 48.0, 2.0));
    }
}
