package com.example.kafkaserver.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderPlaced() {
        return TopicBuilder.name("IN_QUEUE")
                .build();
    }

    @Bean
    public NewTopic shipped() {
        return TopicBuilder.name("SHIPPED")
                .build();
    }

}
