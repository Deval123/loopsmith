package loopsmith.core;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic fallback used when the local model is unavailable or returns something invalid.
 * Understands English and French ("6 km run", "boucle de 6,5 km", "balade ...").
 */
public final class RuleBasedIntentParser implements IntentParser {

    static final double DEFAULT_KM = 5.0;

    private static final Pattern DISTANCE =
            Pattern.compile("(\\d{1,3}(?:[.,]\\d{1,2})?)\\s*(?:km|kms|kilom)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RUN =
            Pattern.compile("\\b(run|running|jog|jogging|course|courir|footing)\\b");

    @Override
    public RouteIntent parse(String prompt) {
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);

        double km = DEFAULT_KM;
        Matcher m = DISTANCE.matcher(text);
        if (m.find()) {
            km = Double.parseDouble(m.group(1).replace(',', '.'));
        }

        Activity activity = RUN.matcher(text).find() ? Activity.RUN : Activity.WALK;
        return new RouteIntent(km, activity);
    }
}
