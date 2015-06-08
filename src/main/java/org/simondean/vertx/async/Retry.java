package org.simondean.vertx.async;

import org.vertx.java.core.AsyncResultHandler;

public interface Retry<T> {
  void run(AsyncResultHandler<T> handler);
}
