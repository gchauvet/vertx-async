package org.simondean.vertx.async.unit.fakes;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

public class FakeSuccessfulTask<T> extends FakeTask<T> {
  private final T result;

  public FakeSuccessfulTask(T result) {
    this.result = result;
  }

  @Override
  public void accept(AsyncResultHandler<T> handler) {
    incrementRunCount();
    handler.handle(new DefaultFutureResult(result));
  }

  public T result() {
    return result;
  }
}
