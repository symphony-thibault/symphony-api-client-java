package com.symphony.bdk.core.service;

import com.symphony.bdk.core.auth.AuthSession;

import org.apiguardian.api.API;

/**
 * Interface which returns an OBO-enabled service class from a given OBO session.
 *
 * @param <S> type returned by the {@link #obo(AuthSession)} function.
 */
@API(status = API.Status.STABLE)
public interface OboService<S> {

  /**
   * Returns a new service instance with OBO-enabled endpoints from a given OBO session.
   *
   * @param oboSession the OBO session
   * @return the instance of the service class with OBO-enabled endpoints
   */
  S obo(AuthSession oboSession);
}
