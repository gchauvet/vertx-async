package org.simondean.vertx.async.internal;

import org.simondean.vertx.async.RetryBuilder;
import org.simondean.vertx.async.RetryTimesBuilder;
import org.vertx.java.core.AsyncResultHandler;

import java.util.function.Consumer;

public class RetryBuilderImpl implements RetryBuilder {
  @Override
  public <T> RetryTimesBuilder<T> task(Consumer<AsyncResultHandler<T>> task) {
    return new RetryTimesBuilderImpl<>(task);
  }
}
