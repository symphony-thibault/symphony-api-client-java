package com.symphony.bdk.core.service.signal;

import com.symphony.bdk.core.auth.AuthSession;
import com.symphony.bdk.core.retry.RetryWithRecovery;
import com.symphony.bdk.core.retry.RetryWithRecoveryBuilder;
import com.symphony.bdk.core.service.OboService;
import com.symphony.bdk.core.service.pagination.PaginatedApi;
import com.symphony.bdk.core.service.pagination.PaginatedService;
import com.symphony.bdk.core.util.function.SupplierWithApiException;
import com.symphony.bdk.gen.api.SignalsApi;
import com.symphony.bdk.gen.api.model.BaseSignal;
import com.symphony.bdk.gen.api.model.ChannelSubscriber;
import com.symphony.bdk.gen.api.model.ChannelSubscriptionResponse;
import com.symphony.bdk.gen.api.model.Signal;
import com.symphony.bdk.http.api.ApiException;

import org.apiguardian.api.API;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Service class for managing signal information.
 * <p>
 * This service is used for listing signals related to the user, get information of a specified signal
 * or perform some actions related to the signal like:
 * <p><ul>
 * <li>Create a signal</li>
 * <li>Update a signal</li>
 * <li>Delete a signal</li>
 * <li>Subscribe or unsubscribe a signal</li>
 * </ul></p>
 */
@API(status = API.Status.STABLE)
public class SignalService implements OboSignalService, OboService<OboSignalService> {

  private final SignalsApi signalsApi;
  private final AuthSession authSession;
  private final RetryWithRecoveryBuilder<?> retryBuilder;

  public SignalService(SignalsApi signalsApi, AuthSession authSession, RetryWithRecoveryBuilder<?> retryBuilder) {
    this.signalsApi = signalsApi;
    this.authSession = authSession;
    this.retryBuilder = retryBuilder;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OboSignalService obo(AuthSession oboSession) {
    return new SignalService(signalsApi, oboSession, retryBuilder);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Signal> listSignals(@Nullable Integer skip, @Nullable Integer limit) {
    return executeAndRetry("listSignals",
        () -> signalsApi.v1SignalsListGet(authSession.getSessionToken(), authSession.getKeyManagerToken(), skip, limit));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Signal> listSignals() {
    return listSignals(null, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<Signal> listSignalsStream(@Nullable Integer chunkSize, @Nullable Integer totalSize) {
    PaginatedApi<Signal> api = ((this::listSignals));

    final int actualChunkSize = chunkSize == null ? 50 : chunkSize;
    final int actualTotalSize = totalSize == null ? 50 : totalSize;

    return new PaginatedService<>(api, actualChunkSize, actualTotalSize).stream();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<Signal> listSignalsStream() {
    return listSignalsStream(null, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Signal getSignal(@Nonnull String id) {
    return executeAndRetry("getSignal",
        () -> signalsApi.v1SignalsIdGetGet(authSession.getSessionToken(), id, authSession.getKeyManagerToken()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Signal createSignal(@Nonnull BaseSignal signal) {
    return executeAndRetry("createSignal",
        () -> signalsApi.v1SignalsCreatePost(authSession.getSessionToken(), signal, authSession.getKeyManagerToken()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Signal updateSignal(@Nonnull String id, @Nonnull BaseSignal signal) {
    return executeAndRetry("updateSignal",
        () -> signalsApi.v1SignalsIdUpdatePost(authSession.getSessionToken(), id, signal, authSession.getKeyManagerToken()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteSignal(@Nonnull String id) {
    executeAndRetry("deleteSignal",
        () -> signalsApi.v1SignalsIdDeletePost(authSession.getSessionToken(), id, authSession.getKeyManagerToken()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ChannelSubscriptionResponse subscribeSignal(@Nonnull String id, @Nullable Boolean pushed, @Nonnull List<Long> userIds) {
    return executeAndRetry("subscribeSignal",
        () -> signalsApi.v1SignalsIdSubscribePost(authSession.getSessionToken(), id, authSession.getKeyManagerToken(), pushed, userIds));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ChannelSubscriptionResponse unsubscribeSignal(@Nonnull String id, @Nullable List<Long> userIds) {
    return executeAndRetry("unsubscribeSignal",
        () -> signalsApi.v1SignalsIdUnsubscribePost(authSession.getSessionToken(), id, authSession.getKeyManagerToken(), userIds));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ChannelSubscriber> listSubscribers(@Nonnull String id, @Nullable Integer skip, @Nullable Integer limit) {
    return executeAndRetry("subscribers",
        () -> signalsApi.v1SignalsIdSubscribersGet(authSession.getSessionToken(), id, authSession.getKeyManagerToken(), skip, limit)).getData();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ChannelSubscriber> listSubscribers(@Nonnull String id) {
    return listSubscribers(id, null, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<ChannelSubscriber> listSubscribersStream(
      @Nonnull String id, @Nullable Integer chunkSize, @Nullable Integer totalSize) {
    PaginatedApi<ChannelSubscriber> api = (((offset, limit) -> listSubscribers(id, offset, limit)));

    final int actualChunkSize = chunkSize == null ? 100 : chunkSize;
    final int actualTotalSize = totalSize == null ? 100 : totalSize;

    return new PaginatedService<>(api, actualChunkSize, actualTotalSize).stream();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<ChannelSubscriber> listSubscribersStream(@Nonnull String id) {
    return listSubscribersStream(id, null, null);
  }

  private <T> T executeAndRetry(String name, SupplierWithApiException<T> supplier) {
    final RetryWithRecoveryBuilder<?> retryBuilderWithAuthSession = RetryWithRecoveryBuilder.from(retryBuilder)
        .clearRecoveryStrategies()
        .recoveryStrategy(ApiException::isUnauthorized, authSession::refresh);
    return RetryWithRecovery.executeAndRetry(retryBuilderWithAuthSession, name, supplier);
  }
}
