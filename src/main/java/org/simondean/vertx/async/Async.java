package org.simondean.vertx.async;

import org.simondean.vertx.async.internal.*;

public final class Async {
  private Async() {}

  public static <T> Series<T> series() {
    return new SeriesImpl<>();
  }

  public static WaterfallBuilder waterfall() {
    return new WaterfallBuilderImpl();
  }

  public static <T> IterableBuilder<T> iterable(Iterable<T> iterable) {
    return new IterableBuilderImpl(iterable);
  }

  public static RetryBuilder retry() {
    return new RetryBuilderImpl();
  }

  public static ForeverBuilder forever() {
    return new ForeverBuilderImpl();
  }
}
