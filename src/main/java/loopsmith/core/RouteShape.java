package loopsmith.core;

import java.util.List;

/** Geometry helpers to tell a real loop from a thin out-and-back. */
public final class RouteShape {

    private static final double EARTH_RADIUS_M = 6_371_000.0;

    private RouteShape() {
    }

    /**
     * Isoperimetric quotient of the track closed on itself: {@code 4 * pi * area / perimeter^2}.
     * A circle scores 1, a square about 0.79, a thin loop that comes back along the same streets
     * scores close to 0. Returns 0 when the shape cannot be measured.
     */
    public static double roundness(List<TrackPoint> points) {
        if (points == null || points.size() < 3) {
            return 0.0;
        }

        double meanLatRad = Math.toRadians(points.stream().mapToDouble(TrackPoint::lat).average().orElse(0));
        double cosLat = Math.cos(meanLatRad);

        int n = points.size();
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = Math.toRadians(points.get(i).lon()) * cosLat * EARTH_RADIUS_M;
            y[i] = Math.toRadians(points.get(i).lat()) * EARTH_RADIUS_M;
        }

        double twiceArea = 0.0;
        double perimeter = 0.0;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n; // the last point connects back to the first
            twiceArea += x[i] * y[j] - x[j] * y[i];
            perimeter += Math.hypot(x[j] - x[i], y[j] - y[i]);
        }

        if (perimeter <= 0.0) {
            return 0.0;
        }
        double area = Math.abs(twiceArea) / 2.0;
        return 4.0 * Math.PI * area / (perimeter * perimeter);
    }
}
