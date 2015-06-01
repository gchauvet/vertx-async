package org.simondean.vertx.async;

import org.simondean.vertx.async.internal.EmptyWaterfallImpl;
import org.simondean.vertx.async.internal.IterableBuilderImpl;
import org.simondean.vertx.async.internal.SeriesImpl;

import java.util.List;

public final class Async {
  private Async() {}

  public static <T> Series<T> series() {
    return new SeriesImpl<>();
  }

  public static EmptyWaterfall waterfall() {
    return new EmptyWaterfallImpl();
  }

  public static <T> IterableBuilder<T> iterable(Iterable<T> iterable) {
    return new IterableBuilderImpl(iterable);
  }
}
