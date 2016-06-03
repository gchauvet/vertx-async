package io.zatarox.vertx.async.internal;

import io.vertx.core.AsyncResult;
import io.zatarox.vertx.async.RetryBuilder;
import io.zatarox.vertx.async.RetryTimesBuilder;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class RetryBuilderImpl implements RetryBuilder {

    @Override
    public <T> RetryTimesBuilder<T> task(Consumer<Handler<AsyncResult<T>>> task) {
        return new RetryTimesBuilderImpl<>(task);
    }
}
