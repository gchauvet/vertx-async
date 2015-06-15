package org.simondean.vertx.async;

import org.vertx.java.core.AsyncResultHandler;

import java.util.function.Consumer;

public interface ForeverBuilder {
  Forever task(Consumer<AsyncResultHandler<Void>> task);
}
