package org.simondean.vertx.async.unit.examples;

import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;

public class ForeverExample extends BaseExample {
  public void foreverExample(AsyncResultHandler<String> handler) {
    Async.forever()
      .task(taskHandler -> {
        someAsyncMethodThatTakesAHandler(taskHandler);
      })
      .run(vertx, result -> {
        handler.handle(DefaultAsyncResult.fail(result));
      });
  }

  private void someAsyncMethodThatTakesAHandler(AsyncResultHandler<Void> handler) {
    handler.handle(DefaultAsyncResult.fail(new Exception("Fail")));
  }
}
