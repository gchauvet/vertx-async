package org.simondean.vertx.async.internal;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.simondean.vertx.async.FunctionWrapper;
import org.simondean.vertx.async.ObjectWrapper;
import org.simondean.vertx.async.Retry;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class RetryImpl<T> implements Retry<T> {

    private final Consumer<Handler<AsyncResult<T>>> task;
    private final int times;

    public RetryImpl(Consumer<Handler<AsyncResult<T>>> task, int times) {
        this.task = task;
        this.times = times;
    }

    @Override
    public void run(Handler<AsyncResult<T>> handler) {
        ObjectWrapper<Integer> count = new ObjectWrapper<>(0);

        FunctionWrapper<Runnable> visitor = new FunctionWrapper<>();
        visitor.wrap(() -> task.accept(result -> {
            if (result.failed()) {
                count.setObject(count.getObject() + 1);

                if (count.getObject() > times) {
                    handler.handle(DefaultAsyncResult.fail(result));
                    return;
                }

                visitor.f().run();
                return;
            }

            handler.handle(DefaultAsyncResult.succeed(result.result()));
        }));

        visitor.f().run();
    }
}
