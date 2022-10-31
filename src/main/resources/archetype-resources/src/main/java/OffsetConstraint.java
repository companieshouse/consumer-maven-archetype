package ${package};

import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Stores the offset number of the message that has been consumed.<br>
 * <br>
 * This class is used internally by {@link ErrorConsumer the error consumer} to determine the most recent offset at the
 * point first message is consumed so that new messages published to the error topic by {@link Consumer the main consumer} are not consumed.
 */
@Component
public class OffsetConstraint {

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * Store the message's offset number.
     *
     * @param offsetConstraint The offset of the consumed message.
     */
    public void setOffsetConstraint(Long offsetConstraint) {
        threadLocal.set(offsetConstraint);
    }

    /**
     * Retrieve the message's offset number.
     *
     * @return The offset of the consumed message.
     */
    public Long getOffsetConstraint() {
        return threadLocal.get();
    }

    @PreDestroy
    void destroy() {
        threadLocal.remove();
    }
}
