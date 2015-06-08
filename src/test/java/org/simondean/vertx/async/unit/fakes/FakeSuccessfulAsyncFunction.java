package org.simondean.vertx.async.unit.fakes;

import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

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
  public void accept(T value, AsyncResultHandler<R> handler) {
    addConsumedValue(value);
    incrementRunCount();

    if (runCount() > failureCount) {
      handler.handle(DefaultAsyncResult.succeed(result));
    }
    else {
      handler.handle(DefaultAsyncResult.fail(cause));
    }
  }

  public R result() {
    return result;
  }
}
