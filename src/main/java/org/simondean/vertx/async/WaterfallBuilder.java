package org.simondean.vertx.async;

import org.vertx.java.core.AsyncResultHandler;

import java.util.function.Consumer;

public interface WaterfallBuilder {
  public <R> Waterfall<R> task(Consumer<AsyncResultHandler<R>> task);
}
