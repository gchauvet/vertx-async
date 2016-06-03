package org.simondean.vertx.async.unit.fakes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class FakeAsyncFunction<T, R> implements BiConsumer<T, Handler<AsyncResult<R>>> {
  private ArrayList<T> consumedValues = new ArrayList<>();
  private int runCount = 0;

  protected void incrementRunCount() {
    runCount++;
  }

  protected void addConsumedValue(T consumedValue) {
    this.consumedValues.add(consumedValue);
  }

  public int runCount() {
    return runCount;
  }

  public T consumedValue() {
    return consumedValues.get(consumedValues.size() - 1);
  }

  public List<T> consumedValues() {
    return consumedValues;
  }
}
