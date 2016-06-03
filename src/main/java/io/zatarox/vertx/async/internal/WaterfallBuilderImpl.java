package io.zatarox.vertx.async.internal;

import io.vertx.core.AsyncResult;
import io.zatarox.vertx.async.WaterfallBuilder;
import io.zatarox.vertx.async.Waterfall;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class WaterfallBuilderImpl implements WaterfallBuilder {

    @Override
    public <T> Waterfall<T> task(Consumer<Handler<AsyncResult<T>>> task) {
        return new DefaultWaterfall<>(task);
    }
}
