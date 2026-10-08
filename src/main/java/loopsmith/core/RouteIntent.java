package loopsmith.core;

/**
 * The only thing the language model is allowed to produce.
 *
 * <p>Whatever the model returns (or whatever a prompt injection makes it return) is funnelled through
 * this validating constructor before anything else happens. The model never picks coordinates and never
 * calls a tool: it cannot influence the route beyond a bounded distance and a two-value activity.
 */
public record RouteIntent(double distanceKm, Activity activity) {

    public static final double MIN_KM = 1.0;
    public static final double MAX_KM = 42.0;

    public RouteIntent {
        if (activity == null) {
            throw new IllegalArgumentException("activity is required");
        }
        if (!Double.isFinite(distanceKm) || distanceKm < MIN_KM || distanceKm > MAX_KM) {
            throw new IllegalArgumentException(
                    "distance must be between " + (int) MIN_KM + " and " + (int) MAX_KM + " km");
        }
    }

    public double distanceMeters() {
        return distanceKm * 1000.0;
    }
}
