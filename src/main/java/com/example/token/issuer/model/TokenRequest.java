package com.example.token.issuer.model;

import java.util.UUID;
import lombok.Data;

/**
 * Data class representing a token request.
 * This class encapsulates the data required to request a token, including a correlation ID, user information,
 * and credential details.
 */

@Data
public class TokenRequest {

  /**
   * Unique correlation ID associated with the token request.
   */
  private UUID correlationId;

  /**
   * User identifier for whom the token is requested.
   */
  private String user;

  /**
   * Credential information required for token issuance.
   */
  private String credential;

}
