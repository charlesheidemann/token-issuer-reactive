package com.example.token.issuer.messaging;

import com.example.token.issuer.model.TokenRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

/**
 * Producer class responsible for sending token request messages to a Kafka topic.
 * This component is responsible for asynchronously sending token request messages to be processed.
 * It utilizes a Reactive Kafka producer template for sending messages.
 */
@Slf4j
@Component
public class TokenRequestProducer {

  /**
   * The name of the Kafka topic to which token request messages are sent.
   */
  private final String topic;

  /**
   * The Reactive Kafka producer template for sending TokenRequest messages.
   */
  private final ReactiveKafkaProducerTemplate<String, TokenRequest> tokenRequestReactiveKafkaProducerTemplate;


  /**
   * Constructs a TokenRequestProducer with the required dependencies.
   */
  public TokenRequestProducer(
      @Value(value = "${REQUEST_TOPIC}") final String topic,
      final ReactiveKafkaProducerTemplate<String, TokenRequest> tokenRequestReactiveKafkaProducerTemplate) {
    this.topic = topic;
    this.tokenRequestReactiveKafkaProducerTemplate = tokenRequestReactiveKafkaProducerTemplate;
  }

  /**
   * Sends a token request message to the configured Kafka topic.
   *
   * @param tokenRequest The TokenRequest message to be sent.
   * @return A Mono representing the sending result, including information about the sent message.
   */
  public Mono<SenderResult<Void>> sendMessage(final TokenRequest tokenRequest) {

    log.info("Sending token request to topic={}, {}={},", topic, TokenRequest.class.getSimpleName(), tokenRequest);

    Message<TokenRequest> message = MessageBuilder
        .withPayload(tokenRequest)
        .setHeader(KafkaHeaders.KEY, tokenRequest.getUser()) // Ensures messages with the same user go to the same partition
        .build();

    return tokenRequestReactiveKafkaProducerTemplate.send(topic, message)
        .doOnSuccess(senderResult -> log.info("Sent {} offset : {}",
            tokenRequest,
            senderResult.recordMetadata().offset()));
  }
}