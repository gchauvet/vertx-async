package org.simondean.vertx.async.unit.fakes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public abstract class FakeAsyncSupplier<T> implements Consumer<Handler<AsyncResult<T>>> {
  private int runCount = 0;

  protected void incrementRunCount() {
    runCount++;
  }

  public int runCount() {
    return runCount;
  }
}
