package org.simondean.vertx.async.unit.fakes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class FakeErrorThrowingAsyncSupplier<T> extends FakeAsyncSupplier<T> {
  private final Error cause;

  public FakeErrorThrowingAsyncSupplier(Error cause) {
    this.cause = cause;
  }

  @Override
  public void accept(Handler<AsyncResult<T>>  handler) {
    incrementRunCount();
    throw cause;
  }

  public Error cause() {
    return cause;
  }
}
