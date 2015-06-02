package org.simondean.vertx.async.unit.examples;

import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

import java.util.List;

public class SeriesExample extends BaseExample {
  private final boolean succeed;
  private List<String> results;

  public SeriesExample(boolean succeed) {
    this.succeed = succeed;
  }

  public void seriesExample(AsyncResultHandler<List<String>> handler) {
    Async.<String>series()
      .task(taskHandler -> {
        String result = getSomeResult();
        taskHandler.handle(DefaultAsyncResult.succeed(result));
      })
      .task(taskHandler -> {
        someAsyncMethodThatTakesAHandler(taskHandler);
      })
      .run(result -> {
        if (result.failed()) {
          handler.handle(DefaultAsyncResult.fail(result));
          return;
        }

        List<String> resultList = result.result();
        doSomethingWithTheResults(resultList);

        handler.handle(DefaultAsyncResult.succeed(resultList));
      });
  }

  private String getSomeResult() {
    return "Result";
  }

  private void someAsyncMethodThatTakesAHandler(AsyncResultHandler<String> handler) {
    if (!succeed) {
      handler.handle(DefaultAsyncResult.fail(new Exception("Fail")));
      return;
    }

    handler.handle(DefaultAsyncResult.succeed("Async result"));
  }

  private void doSomethingWithTheResults(List<String> results) {
    this.results = results;
  }

  public List<String> results() {
    return results;
  }
}
