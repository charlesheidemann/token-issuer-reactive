package com.example.token.issuer.configuration;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

/**
 * Configuration class for setting up Kafka consumers using Spring Kafka and Reactor Kafka.
 * This class defines beans for configuring Kafka consumer options and templates for token request and response topics.
 */

@Slf4j
@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConsumerConfig {

  /**
   * Configures Kafka consumer options for token request messages.
   *
   * @param topic          The Kafka topic for token requests.
   * @param kafkaProperties The Kafka consumer properties.
   * @return ReceiverOptions for token request messages.
   */
  @Bean
  public ReceiverOptions<String, String> tokenRequestReceiverOptions(
      final @Value(value = "${REQUEST_TOPIC}") String topic,
      final KafkaProperties kafkaProperties) {

    Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();

    ReceiverOptions<String, String> basicReceiverOptions = ReceiverOptions.create(consumerProperties);

    return basicReceiverOptions.subscription(Collections.singletonList(topic))
        .addAssignListener(partitions -> log.info("onPartitionsAssigned {}", partitions))
        .addRevokeListener(partitions -> log.info("onPartitionsRevoked {}", partitions));
  }

  /**
   * Creates a ReactiveKafkaConsumerTemplate for token request messages.
   *
   * @param tokenRequestReceiverOptions ReceiverOptions for token request messages.
   * @return A ReactiveKafkaConsumerTemplate for token request messages.
   */
  @Bean
  public ReactiveKafkaConsumerTemplate<String, String> tokenRequestReactiveKafkaConsumerTemplate(
      final ReceiverOptions<String, String> tokenRequestReceiverOptions) {
    return new ReactiveKafkaConsumerTemplate<>(tokenRequestReceiverOptions);
  }

  /**
   * Configures Kafka consumer options for token response messages.
   *
   * @param topic          The Kafka topic for token responses.
   * @param kafkaProperties The Kafka consumer properties.
   * @return ReceiverOptions for token response messages.
   */
  @Bean
  public ReceiverOptions<String, String> tokenResponseReceiverOptions(
      final @Value(value = "${RESPONSE_TOPIC}") String topic,
      final KafkaProperties kafkaProperties) {

    Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();

    // Each consumer will have its own anonymous group ID to consume all messages from the topic without coordination
    // This is essential to build a cache within each service replica.
    String consumerGroup = "anonymous." + UUID.randomUUID();
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);

    ReceiverOptions<String, String> basicReceiverOptions = ReceiverOptions.create(consumerProperties);

    return basicReceiverOptions.subscription(Collections.singletonList(topic))
        .addAssignListener(partitions -> log.info("onPartitionsAssigned {}", partitions))
        .addRevokeListener(partitions -> log.info("onPartitionsRevoked {}", partitions));
  }

  /**
   * Creates a ReactiveKafkaConsumerTemplate for token response messages.
   *
   * @param tokenResponseReceiverOptions ReceiverOptions for token response messages.
   * @return A ReactiveKafkaConsumerTemplate for token response messages.
   */
  @Bean
  public ReactiveKafkaConsumerTemplate<String, String> tokenResponseReactiveKafkaConsumerTemplate(
      final ReceiverOptions<String, String> tokenResponseReceiverOptions) {
    return new ReactiveKafkaConsumerTemplate<>(tokenResponseReceiverOptions);
  }

}