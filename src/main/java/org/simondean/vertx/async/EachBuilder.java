package org.simondean.vertx.async;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Vertx;

public interface EachBuilder {
  void run(Vertx vertx, AsyncResultHandler<Void> handler);
}
