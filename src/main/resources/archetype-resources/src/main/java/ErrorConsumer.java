package ${package};

import java.util.Optional;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Collections;

/**
 * Consumes messages from the configured error Kafka topic.
 */
@Component
public class ErrorConsumer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.NAMESPACE);

    private final KafkaListenerEndpointRegistry registry;
    private final OffsetConstraint offsetConstraint;
    private final String container;
    private final Service service;
    private final MessageFlags messageFlags;

    public ErrorConsumer(KafkaListenerEndpointRegistry registry,
                         OffsetConstraint offsetConstraint,
                         @Value("${error_consumer.group_id}") String container,
                         Service service,
                         MessageFlags messageFlags) {
        this.registry = registry;
        this.offsetConstraint = offsetConstraint;
        this.container = container;
        this.service = service;
        this.messageFlags = messageFlags;
    }

    /**
     * Consume a message from the configured error Kafka topic.<br>
     * <br>
     * On consuming the first message, the consumer retrieves and stores the final offset number of the topic. If this
     * offset number is exceeded, the consumer will be {@link MessageListenerContainer#pause() paused}.<br>
     * <br>
     * An {@link Acknowledgment#acknowledge() acknowledgement} is issued after the meessage is consumed; this is done
     * to prevent the offset immediately after the final offset that was calculated from being processed.
     *
     * @param message A message containing a payload.
     * @param acknowledgment An {@link Acknowledgment acknowledgement handler}.
     */
    @KafkaListener(
            id = "${error_consumer.group_id}",
            containerFactory = "kafkaErrorListenerContainerFactory",
            topics = "${error_consumer.topic}",
            groupId = "${error_consumer.group_id}",
            autoStartup = "${error_consumer.enabled}"
    )
    public void consume(Message<String> message, Acknowledgment acknowledgment) {
        KafkaConsumer<?, ?> consumer = Optional.ofNullable((KafkaConsumer<?, ?>) message.getHeaders().get(KafkaHeaders.CONSUMER))
                .orElseThrow(() -> new NonRetryableException("Missing consumer header"));
        String topic = (String) message.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC);
        Integer partition = Optional.ofNullable((Integer) message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID))
                .orElseThrow(() -> new NonRetryableException("Missing partition header"));
        Long offset = Optional.ofNullable((Long) message.getHeaders().get(KafkaHeaders.OFFSET))
                .orElseThrow(() -> new NonRetryableException("Missing offset header"));
        if (offsetConstraint.getOffsetConstraint() == null) {
            offsetConstraint.setOffsetConstraint(consumer.endOffsets(Collections.singletonList(new TopicPartition(topic, partition))).values().stream().findFirst().orElse(1L) - 1);
        }
        if (offset > offsetConstraint.getOffsetConstraint()) {
            LOGGER.info("Maximum offset exceeded; stopping consumer...");
            Optional.ofNullable(this.registry.getListenerContainer(this.container))
                    .ifPresent(MessageListenerContainer::pause);
        } else {
            try {
                service.processMessage(new ServiceParameters(message.getPayload()));
            } catch (RetryableException e) {
                messageFlags.setRetryable(true);
                throw e;
            } finally {
                acknowledgment.acknowledge();
            }
        }
    }
}
