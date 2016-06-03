package org.simondean.vertx.async.unit.examples;

import org.simondean.vertx.async.unit.fakes.FakeVertx;
import io.vertx.core.Vertx;

public class BaseExample {
  protected Vertx vertx;

  public BaseExample() {
    this.vertx = new FakeVertx();
  }
}
