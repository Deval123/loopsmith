package loopsmith.core;

import java.util.List;
import java.util.Locale;

/** Minimal GPX 1.1 writer. All text is XML-escaped; the file carries no personal data. */
public final class GpxWriter {

    private GpxWriter() {
    }

    public static String write(String name, List<TrackPoint> points) {
        StringBuilder sb = new StringBuilder(64 + points.size() * 64);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gpx version=\"1.1\" creator=\"loopsmith\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        sb.append("  <metadata><name>").append(escape(name)).append("</name></metadata>\n");
        sb.append("  <trk>\n    <name>").append(escape(name)).append("</name>\n    <trkseg>\n");
        for (TrackPoint p : points) {
            sb.append(String.format(Locale.ROOT, "      <trkpt lat=\"%.6f\" lon=\"%.6f\">", p.lat(), p.lon()));
            if (p.ele() != null) {
                sb.append(String.format(Locale.ROOT, "<ele>%.1f</ele>", p.ele()));
            }
            sb.append("</trkpt>\n");
        }
        sb.append("    </trkseg>\n  </trk>\n</gpx>\n");
        return sb.toString();
    }

    static String escape(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
