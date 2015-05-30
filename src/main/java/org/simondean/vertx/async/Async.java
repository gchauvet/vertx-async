package org.simondean.vertx.async;

import org.simondean.vertx.async.internal.EmptyWaterfallImpl;
import org.simondean.vertx.async.internal.SeriesImpl;

public final class Async {
  private Async() {}

  public static <T> Series<T> series() {
    return new SeriesImpl<>();
  }

  public static EmptyWaterfall waterfall() {
    return new EmptyWaterfallImpl();
  }
}
