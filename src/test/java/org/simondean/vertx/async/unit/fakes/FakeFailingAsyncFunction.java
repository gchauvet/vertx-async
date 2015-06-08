package org.simondean.vertx.async.unit.fakes;

import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

public class FakeFailingAsyncFunction<T, R> extends FakeAsyncFunction<T, R> {
  private final int successCount;
  private final R result;
  private final Throwable cause;

  public FakeFailingAsyncFunction(Throwable cause) {
    this(0, null, cause);
  }

  public FakeFailingAsyncFunction(int successCount, R result, Throwable cause) {
    this.successCount = successCount;
    this.result = result;
    this.cause = cause;
  }

  @Override
  public void accept(T value, AsyncResultHandler<R> handler) {
    addConsumedValue(value);
    incrementRunCount();

    if (runCount() > successCount) {
      handler.handle(DefaultAsyncResult.fail(cause));
    }
    else {
      handler.handle(DefaultAsyncResult.succeed(result));
    }
  }

  public Throwable cause() {
    return cause;
  }
}
