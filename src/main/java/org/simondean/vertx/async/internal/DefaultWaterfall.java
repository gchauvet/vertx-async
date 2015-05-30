package org.simondean.vertx.async.internal;

import org.simondean.vertx.async.Waterfall;
import org.vertx.java.core.AsyncResultHandler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultWaterfall<T> implements Waterfall<T> {
  private final Consumer<AsyncResultHandler<T>> task;

  public DefaultWaterfall(Consumer<AsyncResultHandler<T>> task) {
    this.task = task;
  }

  @Override
  public <R> Waterfall<R> task(BiConsumer<T, AsyncResultHandler<R>> task) {
    return new NestedWaterfall<>(this, task);
  }

  @Override
  public void run(AsyncResultHandler<T> handler) {
    if (task == null) {
      handler.handle(null);
      return;
    }

    task.accept(handler);
  }
}
