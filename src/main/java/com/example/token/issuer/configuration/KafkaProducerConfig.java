package com.example.token.issuer.configuration;

import com.example.token.issuer.model.TokenRequest;
import com.example.token.issuer.model.TokenResponse;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.SenderOptions;

/**
 * Configuration class for setting up Kafka producers using Spring Kafka and Reactor Kafka.
 * This class defines beans for configuring Kafka producer options and templates for token request and response topics.
 */

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaProducerConfig {

  /**
   * Configures Kafka producer options for token request messages.
   *
   * @param kafkaProperties Kafka producer properties.
   * @return SenderOptions for token request messages.
   */
  @Bean
  public SenderOptions<String, TokenRequest> tokenRequestProducerProperties(final KafkaProperties kafkaProperties) {
    return SenderOptions.create(kafkaProperties.buildProducerProperties());
  }

  /**
   * Creates a ReactiveKafkaProducerTemplate for token request messages.
   *
   * @param tokenRequestProducerProperties SenderOptions for token request messages.
   * @return A ReactiveKafkaProducerTemplate for token request messages.
   */
  @Bean
  public ReactiveKafkaProducerTemplate<String, TokenRequest> tokenRequestReactiveKafkaProducerTemplate(
      final SenderOptions<String, TokenRequest> tokenRequestProducerProperties) {
    return new ReactiveKafkaProducerTemplate<>(tokenRequestProducerProperties);
  }

  /**
   * Configures Kafka producer options for token response messages.
   *
   * @param kafkaProperties Kafka producer properties.
   * @return SenderOptions for token response messages.
   */
  @Bean
  public SenderOptions<String, TokenResponse> tokenResponseProducerProperties(final KafkaProperties kafkaProperties) {
    Scheduler scheduler = Schedulers.newParallel("producer", 10);
    SenderOptions<String, TokenResponse> senderOptions = SenderOptions.create(kafkaProperties.buildProducerProperties());
    senderOptions = senderOptions.scheduler(scheduler);
    return senderOptions;
  }

  /**
   * Creates a ReactiveKafkaProducerTemplate for token response messages.
   *
   * @param tokenResponseProducerProperties SenderOptions for token response messages.
   * @return A ReactiveKafkaProducerTemplate for token response messages.
   */
  @Bean
  public ReactiveKafkaProducerTemplate<String, TokenResponse> tokenResponseReactiveKafkaProducerTemplate(
      final SenderOptions<String, TokenResponse> tokenResponseProducerProperties) {
    return new ReactiveKafkaProducerTemplate<>(tokenResponseProducerProperties);
  }

}