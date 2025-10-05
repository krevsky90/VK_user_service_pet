package com.krev.notification_service.config;

import com.krev.user_service.dto.UserCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
public class KafkaConsumerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    public final String topicName;

    public KafkaConsumerConfig(@Value("${application.kafka.topic}") String topicName) {
        this.topicName = topicName;
    }

    @Bean
    public ConsumerFactory<String, UserCreatedEvent> consumerFactory(KafkaProperties kafkaProperties) {
        //NOTE: we split parameters into 2 groups:
        // variables - they are in application.yml AND from docker-compose file (like SPRING_KAFKA_BOOTSTRAP_SERVERS parameter)
        var props = kafkaProperties.buildConsumerProperties();
        // and constants that are specified for this app (they are in this code). let's add them to props
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonDeserializer");

        // Spring deserializes incoming messages ONLY to classes of trusted packages!
        // If we do not set trusted packages we will get error like
        //      The class 'com.example.notificationservice.event.UserCreatedEvent'
        //      is not in the trusted packages: [java.util, java.lang].
//        props.put("spring.json.trusted.packages", "*");
        // todo: "*" is not secure. but to set package of UserCreatedEvent class, we need bootleg:
        // notification service should have the same package as it has in user-service module (i.e. com.krev.user_service.dto)
        props.put("spring.json.trusted.packages", "com.krev.user_service.dto");

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 3);   //bulk read by 3 records per 1 poll. NOTE: usually 500 is ok for prod. 3 is only for testing purposes
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 3_000);   //it be used by broker as heartbeat. i.e. if kafka realizes that consumer does not poll => consumer is dead => let's recreate it

        var kafkaConsumerFactory = new DefaultKafkaConsumerFactory<String, UserCreatedEvent>(props);
        return kafkaConsumerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> kafkaListenerContainerFactory(KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(kafkaProperties));
        factory.setBatchListener(true); //to read batches with size = MAX_POLL_RECORDS_CONFIG

        return factory;
    }

}
