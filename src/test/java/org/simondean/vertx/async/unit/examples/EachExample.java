package org.simondean.vertx.async.unit.examples;

import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EachExample extends BaseExample {
  private final boolean succeed;
  private ArrayList<String> items = new ArrayList<>();

  public EachExample(boolean succeed) {
    this.succeed = succeed;
  }

  public void eachExample(AsyncResultHandler<Void> handler) {
    List<String> list = Arrays.asList("one", "two", "three");

    Async.iterable(list)
      .each((item, eachHandler) -> {
        doSomethingWithItem(item, eachHandler);
      })
      .run(vertx, handler);
  }

  private void doSomethingWithItem(String item, AsyncResultHandler<Void> handler) {
    if (!succeed) {
      handler.handle(new DefaultFutureResult<>(new Exception("Fail")));
      return;
    }

    items.add(item);
    handler.handle(DefaultAsyncResult.succeed());
  }

  public List<String> items() {
    return items;
  }
}
