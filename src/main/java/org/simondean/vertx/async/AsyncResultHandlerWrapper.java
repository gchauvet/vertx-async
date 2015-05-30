package org.simondean.vertx.async;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;

public class AsyncResultHandlerWrapper<T, R> implements AsyncResultHandler<R> {
  private final AsyncResultHandler<T> handler;

  public AsyncResultHandlerWrapper(AsyncResultHandler<T> handler) {
    this.handler = handler;
  }

  public static <T, R> AsyncResultHandler<R> wrap(AsyncResultHandler<T> handler) {
    return new AsyncResultHandlerWrapper<>(handler);
  }

  @Override
  public void handle(AsyncResult<R> asyncResult) {
    if (asyncResult.failed()) {
      handler.handle(DefaultAsyncResult.fail(asyncResult.cause()));
      return;
    }

    handler.handle(DefaultAsyncResult.succeed((T) asyncResult.result()));
  }
}
