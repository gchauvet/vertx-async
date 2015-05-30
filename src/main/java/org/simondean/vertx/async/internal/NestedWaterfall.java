package org.simondean.vertx.async.internal;

import org.simondean.vertx.async.DefaultAsyncResult;
import org.simondean.vertx.async.Waterfall;
import org.vertx.java.core.AsyncResultHandler;

import java.util.function.BiConsumer;

public class NestedWaterfall<T, R> implements Waterfall<R> {
  private final Waterfall<T> parentWaterfall;
  private BiConsumer<T, AsyncResultHandler<R>> task;

  public NestedWaterfall(Waterfall<T> parentWaterfall, BiConsumer<T, AsyncResultHandler<R>> task) {
    this.parentWaterfall = parentWaterfall;
    this.task = task;
  }

  @Override
  public <S> Waterfall<S> task(BiConsumer<R, AsyncResultHandler<S>> task) {
    return new NestedWaterfall<>(this, task);
  }

  @Override
  public void run(AsyncResultHandler<R> handler) {
    parentWaterfall.run(result -> {
      if (result.failed()) {
        handler.handle(DefaultAsyncResult.fail(result));
        return;
      }

      task.accept(result.result(), handler);
    });
  }
}
