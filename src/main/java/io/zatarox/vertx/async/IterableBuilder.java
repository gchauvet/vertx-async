package io.zatarox.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.BiConsumer;

public interface IterableBuilder<T> {

    EachBuilder each(BiConsumer<T, Handler<AsyncResult<Void>>> each);
}
