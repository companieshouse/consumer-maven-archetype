package ${package}.service;

import org.springframework.stereotype.Component;
import ${package}.util.ServiceParameters;
import ${package}.exception.NonRetryableException;

/**
 * The default service.
 */
@Component
class NullService implements Service {

    @Override
    public void processMessage(ServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message");
    }
}