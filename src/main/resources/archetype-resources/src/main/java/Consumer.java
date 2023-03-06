package ${package};

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * Consumes messages from the configured main Kafka topic.
 */
@Component
public class Consumer {

    private final Service service;
    private final MessageFlags messageFlags;

    public Consumer(Service service, MessageFlags messageFlags) {
        this.service = service;
        this.messageFlags = messageFlags;
    }

    /**
     * Consume a message from the main Kafka topic.
     *
     * @param message A message containing a payload.
     */
    @KafkaListener(
            id = "${consumer.group_id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = "${consumer.topic}",
            groupId = "${consumer.group_id}",
            autoStartup = "#{!${error_consumer.enabled}}"
    )
    @RetryableTopic(
            attempts = "${consumer.max_attempts}",
            autoCreateTopics = "false",
            backoff = @Backoff(delayExpression = "${consumer.backoff_delay}"),
            retryTopicSuffix = "-${consumer.group_id}-retry",
            dltTopicSuffix = "-${consumer.group_id}-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            fixedDelayTopicStrategy = FixedDelayStrategy.SINGLE_TOPIC,
            include = RetryableException.class
    )
    public void consume(Message<String> message) {
        try {
            service.processMessage(new ServiceParameters(message.getPayload()));
        } catch (RetryableException e) {
            messageFlags.setRetryable(true);
            throw e;
        }
    }
}
