package org.simondean.vertx.async.internal;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.simondean.vertx.async.EachBuilder;
import org.simondean.vertx.async.ObjectWrapper;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class EachBuilderImpl<T> implements EachBuilder {

    private final Iterable<T> iterable;
    private final BiConsumer<T, Handler<AsyncResult<Void>>> each;

    public EachBuilderImpl(Iterable<T> iterable, BiConsumer<T, Handler<AsyncResult<Void>>> each) {
        this.iterable = iterable;
        this.each = each;
    }

    @Override
    public void run(Vertx vertx, Handler<AsyncResult<Void>> handler) {
        final ObjectWrapper<Boolean> failed = new ObjectWrapper<>(false);
        final ObjectWrapper<Integer> finishedCount = new ObjectWrapper<>(0);

        ArrayList<T> items = new ArrayList<T>();

        for (T item : iterable) {
            items.add(item);
        }

        if (items.size() == 0) {
            handler.handle(DefaultAsyncResult.succeed());
            return;
        }

        for (T item : items) {
            vertx.runOnContext(aVoid -> each.accept(item, result -> {
                finishedCount.setObject(finishedCount.getObject() + 1);

                if (result.failed()) {
                    if (!failed.getObject()) {
                        handler.handle(DefaultAsyncResult.fail(result));
                        failed.setObject(true);
                    }

                    return;
                }

                if (finishedCount.getObject() == items.size()) {
                    handler.handle(DefaultAsyncResult.succeed());
                }
            }));

            if (failed.getObject()) {
                return;
            }
        }
    }
}
