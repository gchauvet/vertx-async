package org.simondean.vertx.async;

import org.vertx.java.core.AsyncResultHandler;

import java.util.function.BiConsumer;

public interface Waterfall<T> {
  public <R> Waterfall<R> task(BiConsumer<T, AsyncResultHandler<R>> task);

  public void run(AsyncResultHandler<T> handler);

}
