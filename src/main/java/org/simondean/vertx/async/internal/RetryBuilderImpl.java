package org.simondean.vertx.async.internal;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.RetryBuilder;
import org.simondean.vertx.async.RetryTimesBuilder;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class RetryBuilderImpl implements RetryBuilder {

    @Override
    public <T> RetryTimesBuilder<T> task(Consumer<Handler<AsyncResult<T>>> task) {
        return new RetryTimesBuilderImpl<>(task);
    }
}
