package loopsmith.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuleBasedIntentParserTest {

    private final RuleBasedIntentParser parser = new RuleBasedIntentParser();

    @Test
    void parsesEnglishRun() {
        RouteIntent intent = parser.parse("A 6 km easy run please");
        assertEquals(6.0, intent.distanceKm());
        assertEquals(Activity.RUN, intent.activity());
    }

    @Test
    void parsesFrenchWalkWithDecimalComma() {
        RouteIntent intent = parser.parse("Une balade de 6,5 km");
        assertEquals(6.5, intent.distanceKm());
        assertEquals(Activity.WALK, intent.activity());
    }

    @Test
    void parsesFrenchRunning() {
        assertEquals(Activity.RUN, parser.parse("footing de 8 km").activity());
    }

    @Test
    void usesDefaultsWhenNothingIsStated() {
        RouteIntent intent = parser.parse("something nice");
        assertEquals(RuleBasedIntentParser.DEFAULT_KM, intent.distanceKm());
        assertEquals(Activity.WALK, intent.activity());
    }
}
