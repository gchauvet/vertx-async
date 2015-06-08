package org.simondean.vertx.async;

import org.vertx.java.core.AsyncResultHandler;

import java.util.function.Consumer;

public interface RetryBuilder {
  <T> RetryTimesBuilder<T> task(Consumer<AsyncResultHandler<T>> task);
}
