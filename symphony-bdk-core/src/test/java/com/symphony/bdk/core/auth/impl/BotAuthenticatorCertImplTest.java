package com.symphony.bdk.core.auth.impl;

import com.symphony.bdk.http.api.ApiRuntimeException;
import com.symphony.bdk.core.auth.AuthSession;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.core.test.BdkMockServer;
import com.symphony.bdk.core.test.BdkMockServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for the {@link BotAuthenticatorCertImpl}.
 */
@ExtendWith(BdkMockServerExtension.class)
public class BotAuthenticatorCertImplTest {

  public static final String SESSIONAUTH_AUTHENTICATE_URL = "/sessionauth/v1/authenticate";
  public static final String KEYAUTH_AUTHENTICATE_URL = "/keyauth/v1/authenticate";
  private BotAuthenticatorCertImpl authenticator;

  @BeforeEach
  void init(final BdkMockServer mockServer) {
    this.authenticator = new BotAuthenticatorCertImpl(
        mockServer.newApiClient("/sessionauth"),
        mockServer.newApiClient("/keyauth"));

  }

  @Test
  void testAuthenticateBot(final BdkMockServer mockServer) throws AuthUnauthorizedException {
    mockServer.onPost(SESSIONAUTH_AUTHENTICATE_URL, res -> res.withBody("{ \"token\": \"1234\", \"name\": \"sessionToken\" }"));
    mockServer.onPost(KEYAUTH_AUTHENTICATE_URL, res -> res.withBody("{ \"token\": \"1235\", \"name\": \"keyManagerToken\" }"));

    final AuthSession session = this.authenticator.authenticateBot();
    assertNotNull(session);
    assertEquals(AuthSessionCertImpl.class, session.getClass());
    assertEquals(this.authenticator, ((AuthSessionCertImpl) session).getAuthenticator());
  }

  @Test
  void testRetrieveSessionToken(final BdkMockServer mockServer) throws AuthUnauthorizedException {
    mockServer.onPost(SESSIONAUTH_AUTHENTICATE_URL, res -> res.withBody("{ \"token\": \"1234\", \"name\": \"sessionToken\" }"));

    final String sessionToken = this.authenticator.retrieveSessionToken();
    assertEquals("1234", sessionToken);
  }

  @Test
  void testRetrieveKeyManagerToken(final BdkMockServer mockServer) throws AuthUnauthorizedException {
    mockServer.onPost(KEYAUTH_AUTHENTICATE_URL, res -> res.withBody("{ \"token\": \"1235\", \"name\": \"keyManagerToken\" }"));

    final String sessionToken = this.authenticator.retrieveKeyManagerToken();
    assertEquals("1235", sessionToken);
  }

  @Test
  void testAuthUnauthorizedException(final BdkMockServer mockServer) {
    mockServer.onPost(KEYAUTH_AUTHENTICATE_URL, res -> res.withStatusCode(401));

    assertThrows(AuthUnauthorizedException.class, () -> this.authenticator.retrieveKeyManagerToken());
  }

  @Test
  void testUnknownApiException(final BdkMockServer mockServer) {
    mockServer.onPost(KEYAUTH_AUTHENTICATE_URL, res -> res.withStatusCode(503));

    assertThrows(ApiRuntimeException.class, () -> this.authenticator.retrieveKeyManagerToken());
  }
}
