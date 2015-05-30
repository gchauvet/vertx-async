package org.simondean.vertx.async.unit.fakes;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

public class FakeFailingConsumerTask<T, R> extends FakeConsumerTask<T, R> {
  private final Throwable cause;

  public FakeFailingConsumerTask(Throwable cause) {
    this.cause = cause;
  }

  @Override
  public void accept(T value, AsyncResultHandler<R> handler) {
    setConsumedValue(value);
    incrementRunCount();
    handler.handle(new DefaultFutureResult<>(cause));
  }

  public Throwable cause() {
    return cause;
  }
}
