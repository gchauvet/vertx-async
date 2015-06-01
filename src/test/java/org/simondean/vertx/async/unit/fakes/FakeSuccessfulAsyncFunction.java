package org.simondean.vertx.async.unit.fakes;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

public class FakeSuccessfulAsyncFunction<T, R> extends FakeAsyncFunction<T, R> {
  private final R result;

  public FakeSuccessfulAsyncFunction(R result) {
    this.result = result;
  }

  @Override
  public void accept(T value, AsyncResultHandler<R> handler) {
    addConsumedValue(value);
    incrementRunCount();
    handler.handle(new DefaultFutureResult(this.result));
  }

  public R result() {
    return result;
  }
}
