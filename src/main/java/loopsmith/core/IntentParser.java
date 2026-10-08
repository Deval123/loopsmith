package loopsmith.core;

/** Turns a free-text request into a validated {@link RouteIntent}. */
public interface IntentParser {

    RouteIntent parse(String prompt);
}
