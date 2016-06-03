package org.simondean.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public interface ForeverBuilder {

    Forever task(Consumer<Handler<AsyncResult<Void>>> task);
}
