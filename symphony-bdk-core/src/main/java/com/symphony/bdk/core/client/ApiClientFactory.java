package com.symphony.bdk.core.client;

import com.symphony.bdk.core.client.exception.ApiClientInitializationException;
import com.symphony.bdk.core.client.loadbalancing.DatafeedLoadBalancedApiClient;
import com.symphony.bdk.core.client.loadbalancing.RegularLoadBalancedApiClient;
import com.symphony.bdk.core.config.model.BdkAuthenticationConfig;
import com.symphony.bdk.core.config.model.BdkCertificateConfig;
import com.symphony.bdk.core.config.model.BdkClientConfig;
import com.symphony.bdk.core.config.model.BdkConfig;
import com.symphony.bdk.core.config.model.BdkProxyConfig;
import com.symphony.bdk.core.util.ServiceLookup;
import com.symphony.bdk.http.api.ApiClient;
import com.symphony.bdk.http.api.ApiClientBuilder;
import com.symphony.bdk.http.api.ApiClientBuilderProvider;

import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;

import javax.annotation.Nonnull;

/**
 * Factory responsible for creating {@link ApiClient} instances for each main Symphony's components
 * :
 * <ul>
 *   <li>Agent</li>
 *   <li>KeyManager</li>
 *   <li>Pod</li>
 * </ul>
 */
@Slf4j
@API(status = API.Status.EXPERIMENTAL)
public class ApiClientFactory {

  private final BdkConfig config;
  private final ApiClientBuilderProvider apiClientBuilderProvider;

  public ApiClientFactory(@Nonnull BdkConfig config) {
    this(config, ServiceLookup.lookupSingleService(ApiClientBuilderProvider.class));
  }

  public ApiClientFactory(@Nonnull BdkConfig config, @Nonnull ApiClientBuilderProvider apiClientBuilderProvider) {
    this.config = config;
    this.apiClientBuilderProvider = apiClientBuilderProvider;
  }

  /**
   * Returns a fully initialized {@link ApiClient} for Login API.
   *
   * @return a new {@link ApiClient} instance.
   */
  public ApiClient getLoginClient() {
    return buildClient(this.config.getPod(), "/login");
  }

  /**
   * Returns a fully initialized {@link ApiClient} for Pod API.
   *
   * @return a new {@link ApiClient} instance.
   */
  public ApiClient getPodClient() {
    return buildClient(this.config.getPod(), "/pod");
  }

  /**
   * Returns a fully initialized {@link ApiClient} for KeyManager API.
   *
   * @return a new {@link ApiClient} instance.
   */
  public ApiClient getRelayClient() {
    return buildClient(this.config.getKeyManager(), "/relay");
  }

  /**
   * Returns a fully initialized {@link ApiClient} for Agent API.
   * This may be a {@link RegularLoadBalancedApiClient} or a non load-balanced ApiClient based on the configuration.
   *
   * @return a new {@link ApiClient} instance.
   */
  public ApiClient getAgentClient() {
    if (config.getAgent().getLoadBalancing() != null) {
      return new RegularLoadBalancedApiClient(this.config, this);
    }
    return getRegularAgentClient();
  }

  /**
   * Returns a fully initialized {@link ApiClient} for Agent API to be used by the datafeed services.
   * This may be a {@link DatafeedLoadBalancedApiClient} or a non load-balanced ApiClient based on the configuration.
   *
   * @return a new {@link ApiClient} instance.
   */
  public ApiClient getDatafeedAgentClient() {
    if (config.getAgent().getLoadBalancing() != null) {
      return new DatafeedLoadBalancedApiClient(this.config, this);
    }
    return getRegularAgentClient();
  }

  /**
   * Returns a fully initialized non-load-balanced {@link ApiClient} for Agent API.
   *
   * @return a new {@link ApiClient} instance.
   */
  public ApiClient getRegularAgentClient() {
    return getRegularAgentClient(this.config.getAgent().getBasePath());
  }

  /**
   * Returns a fully initialized non-load-balanced {@link ApiClient} for Agent API given an agent base path.
   *
   * @param agentBasePath the agent base URL to target.
   * @return a new {@link ApiClient} instance.
   */
  public ApiClient getRegularAgentClient(String agentBasePath) {
    return buildClient(agentBasePath + "/agent", this.config.getAgent().getProxy());
  }

  /**
   * Returns a fully initialized {@link ApiClient} for the SessionAuth API. This only works with a
   * certification configured.
   *
   * @return a new {@link ApiClient} instance.
   */
  public ApiClient getSessionAuthClient() {
    return buildClientWithCertificate(this.config.getSessionAuth(), "/sessionauth", this.config.getBot());
  }

  /**
   * Returns a fully initialized {@link ApiClient} for the SessionAuth API using in Extension App Authentication.
   * This only works with a extension app authentication configured
   *
   * @return a new {@link ApiClient} instance.
   */
  public ApiClient getExtAppSessionAuthClient() {
    return buildClientWithCertificate(this.config.getSessionAuth(), "/sessionauth", this.config.getApp());
  }

  /**
   * Returns a fully initialized {@link ApiClient} for the KayAuth API. This only works with a
   * certification configured.
   *
   * @return an new {@link ApiClient} instance.
   */
  public ApiClient getKeyAuthClient() {
    return buildClientWithCertificate(this.config.getKeyManager(), "/keyauth", this.config.getBot());
  }

  private ApiClient buildClient(BdkClientConfig clientConfig, String contextPath) {
    return getApiClientBuilder(clientConfig.getBasePath() + contextPath, clientConfig.getProxy()).build();
  }

  private ApiClient buildClient(String basePath, BdkProxyConfig proxyConfig) {
    return getApiClientBuilder(basePath, proxyConfig).build();
  }

  private ApiClient buildClientWithCertificate(BdkClientConfig clientConfig, String contextPath, BdkAuthenticationConfig config) {
    if (!config.isCertificateAuthenticationConfigured()) {
      throw new ApiClientInitializationException("For certificate authentication, " +
          "certificatePath and certificatePassword must be set");
    }

    final BdkCertificateConfig certificateConfig = config.getCertificateConfig();

    return getApiClientBuilder(clientConfig.getBasePath() + contextPath, clientConfig.getProxy())
        .withKeyStore(certificateConfig.getCertificateBytes(), certificateConfig.getPassword())
        .build();
  }

  private ApiClientBuilder getApiClientBuilder(String basePath, BdkProxyConfig proxyConfig) {
    ApiClientBuilder apiClientBuilder = this.apiClientBuilderProvider
        .newInstance()
        .withBasePath(basePath);

    configureTruststore(apiClientBuilder);
    configureProxy(proxyConfig, apiClientBuilder);

    return apiClientBuilder;
  }

  private void configureTruststore(ApiClientBuilder apiClientBuilder) {
    final BdkCertificateConfig trustStoreConfig = this.config.getSsl().getCertificateConfig();

    if (trustStoreConfig.isConfigured()) {
      apiClientBuilder.withTrustStore(trustStoreConfig.getCertificateBytes(), trustStoreConfig.getPassword());
    }
  }

  private void configureProxy(BdkProxyConfig proxyConfig, ApiClientBuilder apiClientBuilder) {
    if (proxyConfig != null) {
      apiClientBuilder
          .withProxy(proxyConfig.getHost(), proxyConfig.getPort())
          .withProxyCredentials(proxyConfig.getUsername(), proxyConfig.getPassword());
    }
  }
}
