package org.simondean.vertx.async.unit.fakes;

import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

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
  public void accept(AsyncResultHandler<T> handler) {
    incrementRunCount();

    if (runCount() > failureCount) {
      handler.handle(DefaultAsyncResult.succeed(result));
    }
    else {
      handler.handle(DefaultAsyncResult.fail(cause));
    }
  }

  public T result() {
    return result;
  }
}
