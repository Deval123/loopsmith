package loopsmith.routing;

/** The routing engine is unreachable or found no route. The message is safe to log, not to show raw. */
public class RoutingException extends RuntimeException {

    public RoutingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoutingException(String message) {
        super(message);
    }
}
