package org.simondean.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class AsyncResultHandlerWrapper<T, R> implements Handler<AsyncResult<R>> {

    private final Handler<AsyncResult<T>> handler;

    public AsyncResultHandlerWrapper(Handler<AsyncResult<T>> handler) {
        this.handler = handler;
    }

    public static <T, R> Handler<AsyncResult<R>> wrap(Handler<AsyncResult<T>> handler) {
        return new AsyncResultHandlerWrapper<>(handler);
    }

    @Override
    public void handle(AsyncResult<R> asyncResult) {
        if (asyncResult.failed()) {
            handler.handle(DefaultAsyncResult.fail(asyncResult.cause()));
            return;
        }

        handler.handle(DefaultAsyncResult.succeed((T) asyncResult.result()));
    }
}
