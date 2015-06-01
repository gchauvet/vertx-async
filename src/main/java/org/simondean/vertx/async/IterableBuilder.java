package org.simondean.vertx.async;

import org.vertx.java.core.AsyncResultHandler;

import java.util.function.BiConsumer;

public interface IterableBuilder<T> {
  EachBuilder each(BiConsumer<T, AsyncResultHandler<Void>> each);
}
