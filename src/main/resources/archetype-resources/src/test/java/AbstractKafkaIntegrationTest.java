package ${package};

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Testcontainers
@Import(TestKafkaConfig.class)
public abstract class AbstractKafkaIntegrationTest {

    @Container
    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));

    @BeforeAll
    static void configureKafka() {
        try (AdminClient adminClient = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()))) {
            List<NewTopic> topics = List.of(new NewTopic("echo", 1, (short) 1),
                    new NewTopic("echo-echo-consumer-retry", 1, (short) 1),
                    new NewTopic("echo-echo-consumer-error", 1, (short) 1),
                    new NewTopic("echo-echo-consumer-invalid", 1, (short) 1));
            adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}