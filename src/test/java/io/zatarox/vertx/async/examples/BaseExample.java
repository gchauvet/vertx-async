package io.zatarox.vertx.async.examples;

import io.zatarox.vertx.async.fakes.FakeVertx;
import io.vertx.core.Vertx;

public class BaseExample {

    protected Vertx vertx;

    public BaseExample() {
        this.vertx = new FakeVertx();
    }
}
