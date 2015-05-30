package org.simondean.vertx.async;

import org.vertx.java.core.AsyncResultHandler;

import java.util.function.Consumer;

public interface WaterfallBuilder {
  public <T> Waterfall<T> task(Consumer<AsyncResultHandler<T>> task) ;
}
