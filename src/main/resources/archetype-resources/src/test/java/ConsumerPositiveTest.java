package ${package};

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static ${package}.TestUtils.ERROR_TOPIC;
import static ${package}.TestUtils.INVALID_TOPIC;
import static ${package}.TestUtils.MAIN_TOPIC;
import static ${package}.TestUtils.RETRY_TOPIC;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test_main_positive")
class ConsumerPositiveTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private KafkaProducer<String, String> testProducer;
    @Autowired
    private KafkaConsumer<String, String> testConsumer;
    @Autowired
    private CountDownLatch latch;

    @MockBean
    private Service service;

    @BeforeEach
    public void setup() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @Test
    void testConsumeFromMainTopic() throws Exception {

        testProducer.send(new ProducerRecord<>("echo", 0, System.currentTimeMillis(), "key", "value"));
        if (!latch.await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, 10000L, 1);

        //then
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo")).isEqualTo(1);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-echo-consumer-retry")).isZero();
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-echo-consumer-error")).isZero();
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-echo-consumer-invalid")).isZero();
        verify(service).processMessage(new ServiceParameters("value"));
    }
}

