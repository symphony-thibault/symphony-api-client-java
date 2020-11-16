package com.symphony.bdk.core.client.loadbalancing;

import com.symphony.bdk.core.client.ApiClientFactory;
import com.symphony.bdk.core.config.model.BdkConfig;
import com.symphony.bdk.http.api.ApiException;
import com.symphony.bdk.http.api.ApiResponse;
import com.symphony.bdk.http.api.Pair;
import com.symphony.bdk.http.api.util.TypeReference;

import org.apiguardian.api.API;

import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of {@link LoadBalancedApiClient} used in datafeed services
 * where {@link #rotate()} is executed only when explicitly called.
 */
@API(status = API.Status.INTERNAL)
public class DatafeedLoadBalancedApiClient extends LoadBalancedApiClient {

  public DatafeedLoadBalancedApiClient(BdkConfig config, ApiClientFactory apiClientFactory) {
    super(config, apiClientFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> ApiResponse<T> invokeAPI(String path, String method, List<Pair> queryParams, Object body,
      Map<String, String> headerParams, Map<String, String> cookieParams, Map<String, Object> formParams, String accept,
      String contentType, String[] authNames, TypeReference<T> returnType) throws ApiException {

    return apiClient.invokeAPI(path, method, queryParams, body, headerParams, cookieParams, formParams, accept,
        contentType, authNames, returnType);
  }
}
