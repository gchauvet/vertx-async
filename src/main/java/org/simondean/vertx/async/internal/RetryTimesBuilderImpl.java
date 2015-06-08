package org.simondean.vertx.async.internal;

import org.simondean.vertx.async.Retry;
import org.simondean.vertx.async.RetryTimesBuilder;
import org.vertx.java.core.AsyncResultHandler;

import java.util.function.Consumer;

public class RetryTimesBuilderImpl<T> implements RetryTimesBuilder<T> {
  private final Consumer<AsyncResultHandler<T>> task;

  public RetryTimesBuilderImpl(Consumer<AsyncResultHandler<T>> task) {
    this.task = task;
  }

  @Override
  public Retry<T> times(int times) {
    return new RetryImpl<>(task, times);
  }
}
