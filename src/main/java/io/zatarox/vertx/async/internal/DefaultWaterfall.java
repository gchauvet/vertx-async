package io.zatarox.vertx.async.internal;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.zatarox.vertx.async.Waterfall;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultWaterfall<T> implements Waterfall<T> {

    private final Consumer<Handler<AsyncResult<T>>> task;

    public DefaultWaterfall(Consumer<Handler<AsyncResult<T>>> task) {
        this.task = task;
    }

    @Override
    public <R> Waterfall<R> task(BiConsumer<T, Handler<AsyncResult<R>>> task) {
        return new NestedWaterfall<>(this, task);
    }

    @Override
    public void run(Handler<AsyncResult<T>> handler) {
        if (task == null) {
            handler.handle(null);
            return;
        }

        task.accept(handler);
    }
}