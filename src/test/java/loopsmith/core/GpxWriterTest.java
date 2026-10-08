package loopsmith.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class GpxWriterTest {

    @Test
    void writesTrackPointsWithStablePrecision() {
        String gpx = GpxWriter.write("loop", List.of(
                new TrackPoint(48.858400, 2.294500, 35.0),
                new TrackPoint(48.858900, 2.295000, null)));

        assertTrue(gpx.contains("<trkpt lat=\"48.858400\" lon=\"2.294500\"><ele>35.0</ele></trkpt>"));
        assertTrue(gpx.contains("<trkpt lat=\"48.858900\" lon=\"2.295000\"></trkpt>"));
    }

    @Test
    void escapesMarkupInTheName() {
        String gpx = GpxWriter.write("<script>alert('x')</script> & co", List.of());

        assertFalse(gpx.contains("<script>"));
        assertTrue(gpx.contains("&lt;script&gt;"));
        assertTrue(gpx.contains("&amp; co"));
    }
}
