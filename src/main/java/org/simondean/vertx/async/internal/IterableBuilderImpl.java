package org.simondean.vertx.async.internal;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.EachBuilder;
import org.simondean.vertx.async.IterableBuilder;
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
