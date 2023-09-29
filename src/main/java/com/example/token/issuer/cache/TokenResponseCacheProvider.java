package com.example.token.issuer.cache;

import com.example.token.issuer.model.TokenResponse;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks.Many;

/**
 * Configuration class for providing a token response cache using a reactive stream.
 * This class defines a bean for creating and managing a token response cache backed by a ConcurrentHashMap.
 * It also implements the DisposableBean interface to properly dispose of the reactive stream when needed.
 */

@Configuration
public class TokenResponseCacheProvider implements DisposableBean {

  /**
   * The name of the token response cache.
   */
  public static final String TOKEN_RESPONSE_CACHE = "tokenResponseCache";

  /**
   * The reactive stream of TokenResponse objects used to keep the cache updated.
   */
  private final Many<TokenResponse> tokenResponseStream;

  /**
   * Disposable object for managing the stream subscription.
   */
  private Disposable streamDisposable;


  /**
   * Constructs a TokenResponseCacheProvider with the specified token response stream.
   */
  public TokenResponseCacheProvider(final Many<TokenResponse> tokenResponseStream) {
    this.tokenResponseStream = tokenResponseStream;
  }

  /**
   * Creates and configures a token response cache using a ConcurrentHashMap.
   * The cache is backed by the reactive stream and automatically updates itself with new TokenResponse objects.
   *
   * @return A ConcurrentHashMap serving as the token response cache.
   */
  @Bean
  @Qualifier(TOKEN_RESPONSE_CACHE)
  public Map<String, TokenResponse> tokenResponseCache() {
    Map<String, TokenResponse> cache = new ConcurrentHashMap<>() {
      @Override
      public TokenResponse get(Object key) {
        TokenResponse tokenResponse = super.get(key);
        if (Objects.isNull(tokenResponse) || tokenResponse.isExpired(30)) {
          return null;
        }
        return tokenResponse;
      }
    };
    streamDisposable = tokenResponseStream.asFlux()
        .subscribe(tokenResponse -> cache.put(tokenResponse.getUser(), tokenResponse));
    return cache;
  }

  @Override
  public void destroy() throws Exception {
    if (this.streamDisposable != null && !this.streamDisposable.isDisposed()) {
      this.streamDisposable.dispose();
    }
  }

}