package org.simondean.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public interface WaterfallBuilder {
  public <R> Waterfall<R> task(Consumer<Handler<AsyncResult<R>>> task);
}
