package com.example.token.issuer.messaging;

import com.example.token.issuer.model.TokenRequest;
import com.example.token.issuer.model.TokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.SenderResult;

/**
 * A component responsible for processing incoming token request messages from a Kafka topic
 * and producing corresponding token responses to another Kafka topic.
 * It uses reactive programming for more efficient processing.
 */

@Slf4j
@Component
public class TokenRequestProcessor implements DisposableBean {

  /**
   * The name of the Kafka topic to which token response messages are sent.
   */
  private final String topic;

  /**
   * ObjectMapper for JSON serialization and deserialization.
   */
  private final ObjectMapper objectMapper;

  /**
   * Reactive Kafka consumer template for token request messages.
   */
  private final ReactiveKafkaConsumerTemplate<String, String> tokenRequestConsumerTemplate;

  /**
   * Reactive Kafka producer template for token response messages.
   */
  private final ReactiveKafkaProducerTemplate<String, TokenResponse> tokenResponseProducerTemplate;

  /**
   * Disposable object for managing the consumer.
   */
  private Disposable consumerDisposable;


  /**
   * Constructs a TokenRequestProcessor with the required dependencies.
   */
  public TokenRequestProcessor(
      @Value(value = "${RESPONSE_TOPIC}") final String topic,
      final ObjectMapper objectMapper,
      final ReactiveKafkaConsumerTemplate<String, String> tokenRequestReactiveKafkaConsumerTemplate,
      final ReactiveKafkaProducerTemplate<String, TokenResponse> tokenResponseReactiveKafkaProducerTemplate) {
    this.topic = topic;
    this.objectMapper = objectMapper;
    this.tokenRequestConsumerTemplate = tokenRequestReactiveKafkaConsumerTemplate;
    this.tokenResponseProducerTemplate = tokenResponseReactiveKafkaProducerTemplate;
  }

  /**
   * Listens for the ContextRefreshedEvent and initializes the token request consumer.
   *
   * @param event The ContextRefreshedEvent.
   */
  @EventListener
  public void onApplicationEvent(final ContextRefreshedEvent event) {
    log.info("init::tokenRequestConsumer()");
    Scheduler scheduler = Schedulers.newParallel("processor", 10);
    this.consumerDisposable = tokenRequestConsumerTemplate
        .receiveAutoAck()
        .flatMap(receiverRecord -> Mono.fromCallable(() -> process(receiverRecord)).subscribeOn(scheduler))
        .flatMap(this::publishTokenResponse)
        .subscribe();
  }

  /**
   * Processes an incoming token request message received from the Kafka topic.
   * Deserializes the message payload, simulates token issuance and generates a TokenResponse.
   *
   * @param receiverRecord The Kafka ConsumerRecord containing the TokenRequest message.
   * @return The generated token response.
   */
  TokenResponse process(ConsumerRecord<String, String> receiverRecord) {
    log.info("received key={}, value={} from topic={}, offset={}",
        receiverRecord.key(),
        receiverRecord.value(),
        receiverRecord.topic(),
        receiverRecord.offset());

    String payload = receiverRecord.value();

    TokenRequest tokenRequest;
    try {
      tokenRequest = objectMapper.readValue(payload, TokenRequest.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    log.info("successfully consumed {}={}", TokenRequest.class.getSimpleName(), tokenRequest);

    TokenResponse tokenResponse = TokenResponse.fromTokenRequest(tokenRequest);

    try {
      // Token issuance simulation
      TimeUnit.SECONDS.sleep(new Random().nextLong(1, 5));
      tokenResponse.setToken("TOKEN");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    log.info("TokenRequest processed {}", tokenRequest);

    return tokenResponse;
  }

  /**
   * Publishes a token response message to the configured Kafka topic.
   *
   * @param tokenResponse The TokenResponse message to be published.
   * @return A Mono representing the sending result, including information about the sent message.
   */
  Mono<SenderResult<Void>> publishTokenResponse(TokenResponse tokenResponse) {
    Message<TokenResponse> message = MessageBuilder.withPayload(tokenResponse).build();
    return tokenResponseProducerTemplate
        .send(topic, message)
        .doOnSuccess(
            senderResult -> log.info("sent {} offset : {}", tokenResponse, senderResult.recordMetadata().offset()));
  }

  @Override
  public void destroy() throws Exception {
    if (this.consumerDisposable != null && !this.consumerDisposable.isDisposed()) {
      this.consumerDisposable.dispose();
    }
  }

}