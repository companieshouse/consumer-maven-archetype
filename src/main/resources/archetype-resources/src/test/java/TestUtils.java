package ${package};

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public final class TestUtils {

    public static final String MAIN_TOPIC = "echo";
    public static final String RETRY_TOPIC = "echo-echo-consumer-retry";
    public static final String ERROR_TOPIC = "echo-echo-consumer-error";
    public static final String INVALID_TOPIC = "echo-echo-consumer-invalid";

    private TestUtils(){
    }

    public static int noOfRecordsForTopic(ConsumerRecords<?, ?> records, String topic) {
        int count = 0;
        for (ConsumerRecord<?, ?> ignored : records.records(topic)) {
            count++;
        }
        return count;
    }
}
