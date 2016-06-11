package io.zatarox.vertx.async;

import io.zatarox.vertx.async.internal.*;
public final class Async {

    private Async() {
    }

    public static <T> Series<T> series() {
        return new SeriesImpl<>();
    }

    public static WaterfallBuilder waterfall() {
        return new WaterfallBuilderImpl();
    }

    public static RetryBuilder retry() {
        return new RetryBuilderImpl();
    }

    public static ForeverBuilder forever() {
        return new ForeverBuilderImpl();
    }
}
