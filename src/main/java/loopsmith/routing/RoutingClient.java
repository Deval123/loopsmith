package loopsmith.routing;

import loopsmith.core.Activity;

public interface RoutingClient {

    /**
     * A loop starting and ending at the given point, roughly {@code distanceMeters} long.
     * Different seeds give different loops.
     */
    RouteResult roundTrip(double lat, double lon, double distanceMeters, Activity activity, int seed);
}
