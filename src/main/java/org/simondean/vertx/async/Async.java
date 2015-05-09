package org.simondean.vertx.async;

public final class Async {
  private Async() {}

  public static <T> Series<T> series() {
    return new Series<>();
  }
}
