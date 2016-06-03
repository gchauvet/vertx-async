package io.zatarox.vertx.async.internal;

import io.vertx.core.AsyncResult;
import io.zatarox.vertx.async.Forever;
import io.zatarox.vertx.async.ForeverBuilder;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class ForeverBuilderImpl implements ForeverBuilder {

    @Override
    public Forever task(Consumer<Handler<AsyncResult<Void>>> task) {
        return new ForeverImpl(task);
    }
}
