package org.simondean.vertx.async;

import org.simondean.vertx.async.internal.WaterfallBuilderImpl;

public final class Async {
  private Async() {}

  public static <T> Series<T> series() {
    return new Series<>();
  }

  public static WaterfallBuilder waterfall() {
    return new WaterfallBuilderImpl();
  }
}
