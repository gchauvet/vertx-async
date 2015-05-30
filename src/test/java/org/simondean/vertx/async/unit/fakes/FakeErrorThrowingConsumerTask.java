package org.simondean.vertx.async.unit.fakes;

import org.vertx.java.core.AsyncResultHandler;

public class FakeErrorThrowingConsumerTask<T, R> extends FakeConsumerTask<T, R> {
  private final Error cause;

  public FakeErrorThrowingConsumerTask(Error cause) {
    this.cause = cause;
  }

  @Override
  public void accept(T value, AsyncResultHandler<R> handler) {
    setConsumedValue(value);
    incrementRunCount();
    throw cause;
  }

  public Error cause() {
    return cause;
  }
}
