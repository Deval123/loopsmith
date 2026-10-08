package loopsmith.routing;

import java.util.List;
import loopsmith.core.TrackPoint;

public record RouteResult(List<TrackPoint> points, double distanceMeters, double ascendMeters) {
}
