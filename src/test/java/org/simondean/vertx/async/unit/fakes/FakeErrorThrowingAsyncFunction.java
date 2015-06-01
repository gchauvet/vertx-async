package org.simondean.vertx.async.unit.fakes;

import org.vertx.java.core.AsyncResultHandler;

public class FakeErrorThrowingAsyncFunction<T, R> extends FakeAsyncFunction<T, R> {
  private final Error cause;

  public FakeErrorThrowingAsyncFunction(Error cause) {
    this.cause = cause;
  }

  @Override
  public void accept(T value, AsyncResultHandler<R> handler) {
    addConsumedValue(value);
    incrementRunCount();
    throw cause;
  }

  public Error cause() {
    return cause;
  }
}
