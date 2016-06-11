package io.zatarox.vertx.async;

import io.zatarox.vertx.async.internal.*;
public final class Async {

    private Async() {
    }

    public static WaterfallBuilder waterfall() {
        return new WaterfallBuilderImpl();
    }
}
