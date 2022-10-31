package ${package};

/**
 * Processes an incoming message.
 */
public interface Service {

    /**
     * Processes an incoming message.
     *
     * @param parameters Any parameters required when processing the message.
     */
    void processMessage(ServiceParameters parameters);
}