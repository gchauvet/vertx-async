package io.zatarox.vertx.async.fakes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.zatarox.vertx.async.DefaultAsyncResult;

public class FakeSuccessfulAsyncSupplier<T> extends FakeAsyncSupplier<T> {

    private final int failureCount;
    private final T result;
    private final Throwable cause;

    public FakeSuccessfulAsyncSupplier(T result) {
        this(0, null, result);
    }

    public FakeSuccessfulAsyncSupplier(int failureCount, Throwable cause, T result) {
        this.failureCount = failureCount;
        this.result = result;
        this.cause = cause;
    }

    @Override
    public void accept(Handler<AsyncResult<T>> handler) {
        incrementRunCount();

        if (runCount() > failureCount) {
            handler.handle(DefaultAsyncResult.succeed(result));
        } else {
            handler.handle(DefaultAsyncResult.fail(cause));
        }
    }

    public T result() {
        return result;
    }
}
