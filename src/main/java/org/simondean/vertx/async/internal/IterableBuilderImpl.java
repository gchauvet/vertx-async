package org.simondean.vertx.async.internal;

import org.simondean.vertx.async.EachBuilder;
import org.simondean.vertx.async.IterableBuilder;
import org.vertx.java.core.AsyncResultHandler;

import java.util.function.BiConsumer;

public class IterableBuilderImpl<T> implements IterableBuilder<T> {
  private final Iterable<T> iterable;

  public IterableBuilderImpl(Iterable<T> iterable) {
    this.iterable = iterable;
  }

  @Override
  public EachBuilder each(BiConsumer<T, AsyncResultHandler<Void>> each) {
    return new EachBuilderImpl(iterable, each);
  }
}
