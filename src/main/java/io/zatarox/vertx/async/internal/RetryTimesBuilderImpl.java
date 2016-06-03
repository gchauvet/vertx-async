package io.zatarox.vertx.async.internal;

import io.vertx.core.AsyncResult;
import io.zatarox.vertx.async.Retry;
import io.zatarox.vertx.async.RetryTimesBuilder;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class RetryTimesBuilderImpl<T> implements RetryTimesBuilder<T> {

    private final Consumer<Handler<AsyncResult<T>>> task;

    public RetryTimesBuilderImpl(Consumer<Handler<AsyncResult<T>>> task) {
        this.task = task;
    }

    @Override
    public Retry<T> times(int times) {
        return new RetryImpl<>(task, times);
    }
}
