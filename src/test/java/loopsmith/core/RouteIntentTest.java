package loopsmith.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RouteIntentTest {

    @Test
    void acceptsDistancesInsideTheBounds() {
        assertEquals(6000.0, new RouteIntent(6, Activity.RUN).distanceMeters());
        new RouteIntent(RouteIntent.MIN_KM, Activity.WALK);
        new RouteIntent(RouteIntent.MAX_KM, Activity.WALK);
    }

    @Test
    void rejectsOutOfRangeOrNonFiniteDistances() {
        assertThrows(IllegalArgumentException.class, () -> new RouteIntent(0.2, Activity.RUN));
        assertThrows(IllegalArgumentException.class, () -> new RouteIntent(500, Activity.RUN));
        assertThrows(IllegalArgumentException.class, () -> new RouteIntent(Double.NaN, Activity.RUN));
        assertThrows(IllegalArgumentException.class, () -> new RouteIntent(Double.POSITIVE_INFINITY, Activity.RUN));
    }

    @Test
    void rejectsMissingActivity() {
        assertThrows(IllegalArgumentException.class, () -> new RouteIntent(5, null));
    }
}
