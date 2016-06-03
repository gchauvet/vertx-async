package io.zatarox.vertx.async.internal;

import io.vertx.core.AsyncResult;
import io.zatarox.vertx.async.EachBuilder;
import io.zatarox.vertx.async.IterableBuilder;
import io.vertx.core.Handler;

import java.util.function.BiConsumer;

public class IterableBuilderImpl<T> implements IterableBuilder<T> {

    private final Iterable<T> iterable;

    public IterableBuilderImpl(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public EachBuilder each(BiConsumer<T, Handler<AsyncResult<Void>>> each) {
        return new EachBuilderImpl(iterable, each);
    }
}
