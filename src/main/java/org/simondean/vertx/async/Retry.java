package org.simondean.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface Retry<T> {
  void run(Handler<AsyncResult<T>>  handler);
}
