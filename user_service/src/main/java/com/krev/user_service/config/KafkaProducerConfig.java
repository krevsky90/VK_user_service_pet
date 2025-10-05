package com.krev.user_service.config;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.krev.user_service.dto.UserCreatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaProducerConfig.class);

    private final String topicName;

    public KafkaProducerConfig(@Value("${application.kafka.topic}") String topicName) {
        this.topicName = topicName;
    }

    @Bean
    public ProducerFactory<String, UserCreatedEvent> producerFactory(KafkaProperties kafkaProperties) {
        //NOTE: we split parameters into 2 groups:
        // variables - they are in application.yml AND from docker-compose file (like SPRING_KAFKA_BOOTSTRAP_SERVERS parameter)
        var props = kafkaProperties.buildProducerProperties();
        // and constants that are specified for this app (they are in this code). let's add them to props
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //todo: I have no idea why JsonSerializer.class cannot be resolved whereas I've included implementation 'com.fasterxml.jackson.core:jackson-databind' to gradle file
        //so I have to use "org.springframework.kafka.support.serializer.JsonSerializer" as Qwen suggested
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");

        LOGGER.info("Properties:");
        for (Map.Entry<String, Object> e : props.entrySet()) {
            LOGGER.info("\t{" + e.getKey() + ", " + e.getValue() + "}");
        }

        var kafkaProducerFactory = new DefaultKafkaProducerFactory<String, UserCreatedEvent>(props);
        return kafkaProducerFactory;
    }

    @Bean
    public KafkaTemplate<String, UserCreatedEvent> kafkaTemplate(ProducerFactory<String, UserCreatedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // optionally since  topic will be created automatically when producer sends event to the topic.
    // But I don't know how to transfer topicName to UserService (which is producer)
    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }
}