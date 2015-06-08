package org.simondean.vertx.async.unit.fakes;

import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

public class FakeFailingAsyncSupplier<T> extends FakeAsyncSupplier<T> {
  private final int successCount;
  private final T result;
  private final Throwable cause;

  public FakeFailingAsyncSupplier(Throwable cause) {
    this(0, null, cause);
  }

  public FakeFailingAsyncSupplier(int successCount, T result, Throwable cause) {
    this.successCount = successCount;
    this.result = result;
    this.cause = cause;
  }

  @Override
  public void accept(AsyncResultHandler<T> handler) {
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
