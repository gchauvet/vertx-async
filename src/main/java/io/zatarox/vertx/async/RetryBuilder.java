package io.zatarox.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public interface RetryBuilder {

    <T> RetryTimesBuilder<T> task(Consumer<Handler<AsyncResult<T>>> task);
}
