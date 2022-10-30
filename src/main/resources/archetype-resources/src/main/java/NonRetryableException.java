package ${package};

/**
 * An unrecoverable error has occurred, e.g. due to the service being misconfigured or due to invalid data.
 */
public class NonRetryableException extends RuntimeException {

    public NonRetryableException(String message) {
        super(message);
    }

    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
