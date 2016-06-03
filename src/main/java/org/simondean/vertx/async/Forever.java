package org.simondean.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public interface Forever {

    void run(Vertx vertx, Handler<AsyncResult<Void>> handler);
}
