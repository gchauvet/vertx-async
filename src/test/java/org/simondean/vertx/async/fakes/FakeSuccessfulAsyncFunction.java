package org.simondean.vertx.async.fakes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.simondean.vertx.async.DefaultAsyncResult;

public class FakeSuccessfulAsyncFunction<T, R> extends FakeAsyncFunction<T, R> {

    private final int failureCount;
    private final R result;
    private final Throwable cause;

    public FakeSuccessfulAsyncFunction(R result) {
        this(0, null, result);
    }

    public FakeSuccessfulAsyncFunction(int failureCount, Throwable cause, R result) {
        this.failureCount = failureCount;
        this.result = result;
        this.cause = cause;
    }

    @Override
    public void accept(T value, Handler<AsyncResult<R>> handler) {
        addConsumedValue(value);
        incrementRunCount();

        if (runCount() > failureCount) {
            handler.handle(DefaultAsyncResult.succeed(result));
        } else {
            handler.handle(DefaultAsyncResult.fail(cause));
        }
    }

    public R result() {
        return result;
    }
}
