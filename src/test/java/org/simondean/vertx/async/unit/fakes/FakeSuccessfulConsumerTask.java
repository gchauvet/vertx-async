package org.simondean.vertx.async.unit.fakes;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

public class FakeSuccessfulConsumerTask<T, R> extends FakeConsumerTask<T, R> {
  private final R result;

  public FakeSuccessfulConsumerTask(R result) {
    this.result = result;
  }

  @Override
  public void accept(T value, AsyncResultHandler<R> handler) {
    setConsumedValue(value);
    incrementRunCount();
    handler.handle(new DefaultFutureResult(this.result));
  }

  public R result() {
    return result;
  }
}
