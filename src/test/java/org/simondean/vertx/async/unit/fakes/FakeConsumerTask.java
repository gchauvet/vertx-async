package org.simondean.vertx.async.unit.fakes;

import org.vertx.java.core.AsyncResultHandler;

import java.util.function.BiConsumer;

public abstract class FakeConsumerTask<T, R> implements BiConsumer<T, AsyncResultHandler<R>> {
  private T consumedValue;
  private int runCount = 0;

  protected void incrementRunCount() {
    runCount++;
  }

  protected void setConsumedValue(T consumedValue) {
    this.consumedValue = consumedValue;
  }

  public int runCount() {
    return runCount;
  }

  public T consumedValue() {
    return consumedValue;
  }
}
