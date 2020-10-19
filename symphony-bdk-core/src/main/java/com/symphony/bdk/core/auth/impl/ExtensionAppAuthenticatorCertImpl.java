package com.symphony.bdk.core.auth.impl;

import com.symphony.bdk.core.auth.AppAuthSession;
import com.symphony.bdk.core.auth.ExtensionAppTokensRepository;
import com.symphony.bdk.core.auth.exception.AuthInitializationException;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.core.auth.impl.model.ExtensionAppAuthenticateRequest;
import com.symphony.bdk.core.auth.jwt.JwtHelper;
import com.symphony.bdk.core.auth.jwt.UserClaim;
import com.symphony.bdk.gen.api.CertificateAuthenticationApi;
import com.symphony.bdk.gen.api.CertificatePodApi;
import com.symphony.bdk.gen.api.model.ExtensionAppTokens;
import com.symphony.bdk.gen.api.model.PodCertificate;
import com.symphony.bdk.http.api.ApiClient;
import com.symphony.bdk.http.api.ApiException;
import com.symphony.bdk.http.api.ApiRuntimeException;

import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;

/**
 * Extension app authenticator RSA implementation.
 *
 * @see <a href="https://developers.symphony.com/extension/docs/application-authentication#section-verifying-decoding-and-using-the-jwt">Application Authentication</a>
 */
@Slf4j
@API(status = API.Status.INTERNAL)
public class ExtensionAppAuthenticatorCertImpl extends AbstractExtensionAppAuthenticator{

  private final CertificateAuthenticationApi certificateAuthenticationApi;
  private final CertificatePodApi certificatePodApi;

  public ExtensionAppAuthenticatorCertImpl(String appId, ApiClient sessionAuthClient) {
    super(appId);
    this.certificateAuthenticationApi = new CertificateAuthenticationApi(sessionAuthClient);
    this.certificatePodApi = new CertificatePodApi(sessionAuthClient);
  }

  public ExtensionAppAuthenticatorCertImpl(String appId, ApiClient sessionAuthClient,
      ExtensionAppTokensRepository tokensRepository) {
    super(appId, tokensRepository);
    this.certificateAuthenticationApi = new CertificateAuthenticationApi(sessionAuthClient);
    this.certificatePodApi = new CertificatePodApi(sessionAuthClient);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AppAuthSession authenticateExtensionApp(String appToken) throws AuthUnauthorizedException {
    AppAuthSession authSession = new AppAuthSessionCertImpl(this, appToken);
    authSession.refresh();
    return authSession;
  }

  @Override
  protected ExtensionAppTokens retrieveExtAppTokens(String appToken) throws ApiException {
    final ExtensionAppAuthenticateRequest authRequest = new ExtensionAppAuthenticateRequest().appToken(appToken);
    return this.certificateAuthenticationApi.v1AuthenticateExtensionAppPost(authRequest);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UserClaim validateJwt(String jwt) throws AuthInitializationException {
    return JwtHelper.validateJwt(jwt, this.getPodCertificate().getCertificate());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PodCertificate getPodCertificate() {
    try {
      return this.certificatePodApi.v1AppPodCertificateGet();
    } catch (ApiException e) {
      throw new ApiRuntimeException(e);
    }
  }
}
