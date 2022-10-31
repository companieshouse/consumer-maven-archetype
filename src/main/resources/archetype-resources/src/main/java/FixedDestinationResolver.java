package ${package};

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Routes a message to a dead-letter topic.
 */
@Component
public class FixedDestinationResolver {

    private final String topic;

    public FixedDestinationResolver(@Value("${topic.retry}") String topic) {
        this.topic = topic;
    }

    /**
     * Route a message to a dead-letter topic.
     *
     * @param consumerRecord The message that was consumed from Kafka.
     * @param exception The exception that was thrown.
     * @return A {@link TopicPartition} referring to the first partition of the configured retry topic.
     */
    public TopicPartition resolve(ConsumerRecord<?, ?> consumerRecord, Exception exception) {
        return new TopicPartition(this.topic, 0);
    }
}
