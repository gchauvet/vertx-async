package org.simondean.vertx.async.internal;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.simondean.vertx.async.Waterfall;
import io.vertx.core.Handler;

import java.util.function.BiConsumer;

public class NestedWaterfall<T, R> implements Waterfall<R> {
  private final Waterfall<T> parentWaterfall;
  private BiConsumer<T, Handler<AsyncResult<R>>> task;

  public NestedWaterfall(Waterfall<T> parentWaterfall, BiConsumer<T, Handler<AsyncResult<R>>> task) {
    this.parentWaterfall = parentWaterfall;
    this.task = task;
  }

  @Override
  public <S> Waterfall<S> task(BiConsumer<R, Handler<AsyncResult<S>>> task) {
    return new NestedWaterfall<>(this, task);
  }

  @Override
  public void run(Handler<AsyncResult<R>> handler) {
    parentWaterfall.run(result -> {
      if (result.failed()) {
        handler.handle(DefaultAsyncResult.fail(result));
        return;
      }

      task.accept(result.result(), handler);
    });
  }
}
