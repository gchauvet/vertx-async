package org.simondean.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.function.Consumer;

public interface Series<T> {
  Series<T> task(Consumer<Handler<AsyncResult<T>>> task);

  void run(Handler<AsyncResult<List<T>>> handler);
}