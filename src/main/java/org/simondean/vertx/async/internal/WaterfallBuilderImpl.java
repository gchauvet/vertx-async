package org.simondean.vertx.async.internal;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.WaterfallBuilder;
import org.simondean.vertx.async.Waterfall;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class WaterfallBuilderImpl implements WaterfallBuilder {

    @Override
    public <T> Waterfall<T> task(Consumer<Handler<AsyncResult<T>>> task) {
        return new DefaultWaterfall<>(task);
    }
}
