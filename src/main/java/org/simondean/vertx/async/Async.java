package org.simondean.vertx.async;

import org.simondean.vertx.async.internal.WaterfallBuilderImpl;
import org.simondean.vertx.async.internal.IterableBuilderImpl;
import org.simondean.vertx.async.internal.SeriesImpl;

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
}
