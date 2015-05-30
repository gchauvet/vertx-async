package org.simondean.vertx.async.unit.examples;

import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

public class WaterfallExample {
  private final boolean succeed;
  private Integer result;

  public WaterfallExample(boolean succeed) {
    this.succeed = succeed;
  }

  public void waterfallExample(AsyncResultHandler<Integer> handler) {
    Async.waterfall()
      .<String>task(taskHandler -> {
        String result = getSomeResult();
        taskHandler.handle(DefaultAsyncResult.succeed(result));
      })
      .<Integer>task((result, taskHandler) -> {
        someAsyncMethodThatTakesAResultAndHandler(result, taskHandler);
      })
      .run(result -> {
        if (result.failed()) {
          handler.handle(DefaultAsyncResult.fail(result.cause()));
          return;
        }

        Integer resultValue = result.result();
        doSomethingWithTheResults(resultValue);

        handler.handle(DefaultAsyncResult.succeed(resultValue));
      });
  }

  private String getSomeResult() {
    return "42";
  }

  private void someAsyncMethodThatTakesAResultAndHandler(String result, AsyncResultHandler<Integer> handler) {
    if (succeed) {
      handler.handle(new DefaultFutureResult<>(Integer.parseInt(result)));
    }
    else {
      handler.handle(new DefaultFutureResult<>(new Exception("Fail")));
    }
  }

  private void doSomethingWithTheResults(Integer result) {
    this.result = result;
  }

  public Integer result() {
    return result;
  }
}
