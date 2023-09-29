package com.example.token.issuer.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Data class representing a token response.
 * This class encapsulates the data related to a token, including a correlation ID, user information,
 * the issued token itself, and its expiration time.
 */
@Data
@AllArgsConstructor
@Builder
public class TokenResponse {

  /**
   * A default TokenResponse instance.
   */
  public static final TokenResponse DEFAULT = new TokenResponse(
      UUID.fromString("00000000-0000-0000-0000-000000000000"),
      "user",
      "token",
      Instant.ofEpochMilli(0));

  /**
   * Unique correlation ID associated with the token response.
   */
  private UUID correlationId;

  /**
   * User identifier for whom the token is issued.
   */
  private String user;

  /**
   * The issued token.
   */
  private String token;

  /**
   * The expiration time of the token.
   */
  private Instant expiresAt;

  /**
   * Sets the token value and updates the expiration time based on the token's validity.
   *
   * @param token The token string.
   */
  public void setToken(String token) {
    this.token = token;
    // Update the expiration time using the token's validity period.
    this.expiresAt = Instant.now().plusSeconds(60); // Assuming a default validity period of 60 seconds.
  }

  /**
   * Checks if the token is expired, considering a graceful period.
   *
   * @param gracefulPeriod The additional time, in seconds, to consider the token as not expired.
   * @return true if the token is expired, false otherwise.
   */
  public boolean isExpired(long gracefulPeriod) {
    return Objects.nonNull(this.expiresAt) && this.expiresAt.isBefore(Instant.now().plusSeconds(gracefulPeriod));
  }

  /**
   * Creates a TokenResponse instance from a TokenRequest by copying user and correlation ID.
   *
   * @param tokenRequest The TokenRequest to derive data from.
   * @return A TokenResponse instance based on the TokenRequest.
   */
  public static TokenResponse fromTokenRequest(final TokenRequest tokenRequest) {
    return TokenResponse.builder()
        .user(tokenRequest.getUser())
        .correlationId(tokenRequest.getCorrelationId())
        .build();
  }

}