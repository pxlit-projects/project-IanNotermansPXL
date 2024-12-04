package be.pxl.service;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *
 * Messaging Service Application
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MessagingServiceApplication {

    private final RabbitAdmin rabbitAdmin;

    public MessagingServiceApplication(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public static void main(String[] args) {
        SpringApplication.run(MessagingServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Explicitly trigger RabbitAdmin to declare all beans (queues, exchanges, etc.)
        rabbitAdmin.initialize();
    }
}
