package com.example.token.issuer.configuration;

import com.example.token.issuer.model.TokenResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

/**
 * Configuration class for creating and managing reactive streams used within the token issuer application.
 * This class defines a bean for creating a reactive Sinks.Many stream for TokenResponse objects.
 */

@Configuration
public class StreamsConfig {

  /**
   * Creates a reactive Sinks.Many stream for TokenResponse objects. This stream allows multiple subscribers to receive
   * and process TokenResponse data.
   *
   * @return A Sinks.Many stream for TokenResponse objects.
   */
  @Bean
  public Many<TokenResponse> tokenResponseStream() {
    return Sinks.many().replay().latestOrDefault(TokenResponse.DEFAULT);
  }

}