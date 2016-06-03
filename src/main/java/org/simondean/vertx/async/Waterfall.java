package org.simondean.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.BiConsumer;

public interface Waterfall<T> {

    public <R> Waterfall<R> task(BiConsumer<T, Handler<AsyncResult<R>>> task);

    public void run(Handler<AsyncResult<T>> handler);
}
