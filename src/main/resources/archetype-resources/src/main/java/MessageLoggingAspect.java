package ${package};

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Logs message details before and after it has been processed by
 * the {@link Consumer main consumer} or {@link ErrorConsumer error consumer}.<br>
 * <br>
 * Details that will be logged will include:
 * <ul>
 *     <li>The context ID of the message</li>
 *     <li>The topic the message was consumed from</li>
 *     <li>The partition of the topic the message was consumed from</li>
 *     <li>The offset number of the message</li>
 * </ul>
 */
@Component
@Aspect
public class MessageLoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.NAMESPACE);

    private static final String LOG_MESSAGE_RECEIVED = "Processing delta";
    private static final String LOG_MESSAGE_PROCESSED = "Processed delta";

    @Before("execution(* ${package}.Consumer.consume(..))")
    void logBeforeMainConsumer(JoinPoint joinPoint) {
        logMessage(LOG_MESSAGE_RECEIVED, (Message<?>)joinPoint.getArgs()[0]);
    }

    @After("execution(* ${package}.Consumer.consume(..))")
    void logAfterMainConsumer(JoinPoint joinPoint) {
        logMessage(LOG_MESSAGE_PROCESSED, (Message<?>)joinPoint.getArgs()[0]);
    }

    @Before("execution(* ${package}.ErrorConsumer.consume(..))")
    void logBeforeErrorConsumer(JoinPoint joinPoint) {
        logMessage(LOG_MESSAGE_RECEIVED, (Message<?>)joinPoint.getArgs()[0]);
    }

    @After("execution(* ${package}.ErrorConsumer.consume(..))")
    void logAfterErrorConsumer(JoinPoint joinPoint) {
        logMessage(LOG_MESSAGE_PROCESSED, (Message<?>)joinPoint.getArgs()[0]);
    }

    private void logMessage(String logMessage, Message<?> incomingMessage) {
        String topic = Optional.ofNullable((String) incomingMessage.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC))
                .orElse("no topic");
        Integer partition = Optional.ofNullable((Integer) incomingMessage.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID))
                .orElse(0);
        Long offset = Optional.ofNullable((Long) incomingMessage.getHeaders().get(KafkaHeaders.OFFSET))
                .orElse(0L);
        Map<String, Object> logData = Map.of(
                "topic", topic,
                "partition", partition,
                "offset", offset);
        LOGGER.debug(logMessage, logData);
    }
}
