package ${package};

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestKafkaConfig {

    @Bean
    CountDownLatch latch(@Value("${steps}") int steps) {
        return new CountDownLatch(steps);
    }

    @Bean
    KafkaConsumer<String, String> testConsumer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false",
                        ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString()),
                new StringDeserializer(), new StringDeserializer());
        kafkaConsumer.subscribe(List.of("echo",
                "echo-echo-consumer-error",
                "echo-echo-consumer-retry",
                "echo-echo-consumer-invalid"));
        return kafkaConsumer;
    }

    @Bean
    KafkaProducer<String, String> testProducer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaProducer<>(
                Map.of(
                        ProducerConfig.ACKS_CONFIG, "all",
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                new StringSerializer(), new StringSerializer());
    }
}