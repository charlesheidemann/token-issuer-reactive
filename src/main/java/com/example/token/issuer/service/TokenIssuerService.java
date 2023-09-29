package com.example.token.issuer.service;

import com.example.token.issuer.cache.TokenResponseCacheProvider;
import com.example.token.issuer.messaging.TokenRequestProducer;
import com.example.token.issuer.model.TokenRequest;
import com.example.token.issuer.model.TokenResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks.Many;

/**
 * Service class responsible for token issuance.
 * This class processes incoming token requests, checks if the requested token is already produced and cached,
 * if not interacts with the TokenRequestProducer to send requests for token issuance.
 * It also manages a reactive stream of token responses.
 *
 * This service optimizes token retrieval by first checking the cache for previously issued tokens.
 * If a cached token is found, it is returned immediately, avoiding unnecessary requests.
 * If the requested token is not cached, the service initiates a request for token issuance
 * and waits for a response from the reactive stream for a specified duration of time.
 *
 * This service utilizes reactive programming to handle token issuance asynchronously.
 */
@Slf4j
@Service
public class TokenIssuerService {

  /**
   * The token response cache storing previously issued tokens.
   */
  public final Map<String, TokenResponse> tokenResponseCache;

  /**
   * The producer responsible for sending token request messages.
   */
  private final TokenRequestProducer tokenRequestProducer;

  /**
   * The reactive stream for token responses.
   */
  private final Many<TokenResponse> tokenResponseStream;

  /**
   * Constructs a TokenIssuerService with the necessary dependencies.
   */
  public TokenIssuerService(
      @Qualifier(TokenResponseCacheProvider.TOKEN_RESPONSE_CACHE) final Map<String, TokenResponse> tokenResponseCache,
      final TokenRequestProducer tokenRequestProducer,
      final Many<TokenResponse> tokenResponseStream) {
    this.tokenResponseCache = tokenResponseCache;
    this.tokenRequestProducer = tokenRequestProducer;
    this.tokenResponseStream = tokenResponseStream;
  }

  /**
   * Processes a token request, attempting to retrieve a cached token response
   * or requesting a new token through the producer.
   *
   * @param tokenRequest The incoming token request.
   * @return A Mono containing the TokenResponse for the request.
   */
  public Mono<TokenResponse> process(final TokenRequest tokenRequest) {

    // Check if the requested token is cached.
    TokenResponse tokenResponseCached = tokenResponseCache.get(tokenRequest.getUser());
    if (Objects.nonNull(tokenResponseCached)) {
      return Mono.just(tokenResponseCached);
    }

    // Create a Mono to await a token response from the reactive stream.
    Mono<TokenResponse> tokenResponseMono = tokenResponseStream.asFlux()
        .takeUntil(tokenResponse -> Objects.equals(tokenRequest.getCorrelationId(), tokenResponse.getCorrelationId()))
        .timeout(Duration.ofSeconds(30)) // Wait for up to 30 seconds for a token to be issued.
        .last();

    // Send the token request and return the resulting token response or throw a timeout exception.
    return tokenRequestProducer.sendMessage(tokenRequest).then(tokenResponseMono);
  }

}