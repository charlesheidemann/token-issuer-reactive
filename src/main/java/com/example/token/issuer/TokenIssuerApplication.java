package com.example.token.issuer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * The main entry point for the Token Issuer application.
 * This class initializes and configures the Spring Boot application, excluding the Kafka auto-configuration.
 * It enables WebFlux for reactive web support.
 */
@Slf4j
@EnableWebFlux
@SpringBootApplication(exclude = {
    KafkaAutoConfiguration.class
})
public class TokenIssuerApplication {

  /**
   * The main method to start the Token Issuer application.
   *
   * @param args The command-line arguments (if any).
   */
  public static void main(String[] args) {
    SpringApplication.run(TokenIssuerApplication.class, args);
  }
}