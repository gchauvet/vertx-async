package io.zatarox.vertx.async.fakes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class FakeErrorThrowingAsyncFunction<T, R> extends FakeAsyncFunction<T, R> {

    private final Error cause;

    public FakeErrorThrowingAsyncFunction(Error cause) {
        this.cause = cause;
    }

    @Override
    public void accept(T value, Handler<AsyncResult<R>> handler) {
        addConsumedValue(value);
        incrementRunCount();
        throw cause;
    }

    public Error cause() {
        return cause;
    }
}
