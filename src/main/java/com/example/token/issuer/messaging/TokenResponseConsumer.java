package com.example.token.issuer.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.token.issuer.model.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;

@Slf4j
@Component
public class TokenResponseConsumer implements DisposableBean {

  private final ReactiveKafkaConsumerTemplate<String, String> tokenResponseConsumerTemplate;

  private final ObjectMapper objectMapper;

  private final Many<TokenResponse> tokenResponseStream;

  private Disposable consumerDisposable;

  public TokenResponseConsumer(
      final ReactiveKafkaConsumerTemplate<String, String> tokenResponseReactiveKafkaConsumerTemplate,
      final Many<TokenResponse> tokenResponseStream,
      final ObjectMapper objectMapper) {
    this.tokenResponseConsumerTemplate = tokenResponseReactiveKafkaConsumerTemplate;
    this.objectMapper = objectMapper;
    this.tokenResponseStream = tokenResponseStream;
  }

  @EventListener
  public void onApplicationEvent(final ContextRefreshedEvent event) {
    log.info("init::tokenResponseConsumer()");
    this.consumerDisposable = tokenResponseConsumerTemplate
        .receiveAutoAck()
        .doOnNext(consumerRecord -> log.info("received key={}, value={} from topic={}, offset={}",
            consumerRecord.key(),
            consumerRecord.value(),
            consumerRecord.topic(),
            consumerRecord.offset())
        )
        .map(ConsumerRecord::value)
        .map(payload -> {
          TokenResponse tokenResponse;
          try {
            tokenResponse = objectMapper.readValue(payload, TokenResponse.class);
            this.tokenResponseStream.emitNext(tokenResponse, EmitFailureHandler.FAIL_FAST);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
          log.info("successfully consumed {}={}", TokenResponse.class.getSimpleName(), tokenResponse);
          return tokenResponse;
        })
        .doOnError(throwable -> log.error("something went wrong while consuming : {}", throwable.getMessage()))
        .subscribe();
  }

  @Override
  public void destroy() throws Exception {
    if (this.consumerDisposable != null && !this.consumerDisposable.isDisposed()) {
      this.consumerDisposable.dispose();
    }
  }

}