package loopsmith.core;

/** One point of a track. {@code ele} is metres above sea level, or {@code null} when unknown. */
public record TrackPoint(double lat, double lon, Double ele) {
}
