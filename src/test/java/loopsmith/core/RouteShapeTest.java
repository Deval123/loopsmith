package loopsmith.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RouteShapeTest {

    private static TrackPoint p(double lat, double lon) {
        return new TrackPoint(lat, lon, null);
    }

    /** Rectangle of the given size in degrees. */
    private static List<TrackPoint> rectangle(double dLat, double dLon) {
        return List.of(p(48.0, 2.0), p(48.0 + dLat, 2.0), p(48.0 + dLat, 2.0 + dLon), p(48.0, 2.0 + dLon));
    }

    @Test
    void aCircleScoresCloseToOne() {
        List<TrackPoint> circle = new ArrayList<>();
        for (int i = 0; i < 360; i += 5) {
            double a = Math.toRadians(i);
            circle.add(p(48.0 + 0.01 * Math.sin(a), 2.0 + 0.01 / Math.cos(Math.toRadians(48.0)) * Math.cos(a)));
        }
        assertEquals(1.0, RouteShape.roundness(circle), 0.01);
    }

    @Test
    void aSquareBeatsAThinRectangleOfTheSamePerimeter() {
        double lonPerLat = 1 / Math.cos(Math.toRadians(48.0));
        List<TrackPoint> square = rectangle(0.01, 0.01 * lonPerLat);
        List<TrackPoint> thin = rectangle(0.019, 0.001 * lonPerLat);

        assertEquals(Math.PI / 4, RouteShape.roundness(square), 0.01);
        assertTrue(RouteShape.roundness(square) > 3 * RouteShape.roundness(thin));
    }

    @Test
    void anOutAndBackScoresZero() {
        List<TrackPoint> outAndBack = List.of(p(48.0, 2.0), p(48.01, 2.0), p(48.02, 2.0), p(48.01, 2.0));
        assertEquals(0.0, RouteShape.roundness(outAndBack), 1e-9);
    }

    @Test
    void unmeasurableShapesScoreZero() {
        assertEquals(0.0, RouteShape.roundness(List.of()));
        assertEquals(0.0, RouteShape.roundness(List.of(p(48.0, 2.0), p(48.1, 2.1))));
        assertEquals(0.0, RouteShape.roundness(List.of(p(48.0, 2.0), p(48.0, 2.0), p(48.0, 2.0))));
    }
}
