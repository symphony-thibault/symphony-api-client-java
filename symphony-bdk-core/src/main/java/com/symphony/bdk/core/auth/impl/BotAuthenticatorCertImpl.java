package com.symphony.bdk.core.auth.impl;

import com.symphony.bdk.http.api.ApiClient;
import com.symphony.bdk.http.api.ApiException;
import com.symphony.bdk.http.api.ApiRuntimeException;
import com.symphony.bdk.core.auth.AuthSession;
import com.symphony.bdk.core.auth.BotAuthenticator;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.gen.api.CertificateAuthenticationApi;
import com.symphony.bdk.gen.api.model.Token;

import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;

import java.net.HttpURLConnection;

import javax.annotation.Nonnull;

/**
 * Bot authenticator certificate implementation.
 *
 * @see <a href="https://developers.symphony.com/symphony-developer/docs/bot-authentication-workflow-1">BotAuthentication Workflow</a>
 */
@Slf4j
@API(status = API.Status.INTERNAL)
public class BotAuthenticatorCertImpl implements BotAuthenticator {

  private final ApiClient sessionAuthClient;
  private final ApiClient keyAuthClient;

  public BotAuthenticatorCertImpl(@Nonnull ApiClient sessionAuthClient,
      @Nonnull ApiClient keyAuthClient) {
    this.sessionAuthClient = sessionAuthClient;
    this.keyAuthClient = keyAuthClient;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AuthSession authenticateBot() throws AuthUnauthorizedException {
    AuthSessionCertImpl authSession = new AuthSessionCertImpl(this);
    authSession.refresh();
    return authSession;
  }

  protected @Nonnull String retrieveSessionToken() throws AuthUnauthorizedException {
    log.debug("Start retrieving sessionToken using certificate authentication...");
    return doRetrieveToken(this.sessionAuthClient);
  }

  protected  @Nonnull String retrieveKeyManagerToken() throws AuthUnauthorizedException {
    log.debug("Start retrieving keyManagerToken using certificate authentication...");
    return doRetrieveToken(this.keyAuthClient);
  }

  private String doRetrieveToken(ApiClient client) throws AuthUnauthorizedException {
    try {
      final Token token = new CertificateAuthenticationApi(client).v1AuthenticatePost();
      log.debug("{} successfully retrieved.", token.getName());
      return token.getToken();
    } catch (ApiException ex) {
      if (ex.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
        // usually happens when the certificate is not correct
        throw new AuthUnauthorizedException(
            "Service account is not authorized to authenticate using certificate. " +
                "Please check if certificate is correct.", ex);
      } else {
        // we don't know what to do, let's forward the ApiException
        throw new ApiRuntimeException(ex);
      }
    }
  }

}
