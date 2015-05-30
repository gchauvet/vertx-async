package org.simondean.vertx.async.unit.examples;

import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

import java.util.List;

public class SeriesExample {
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
          handler.handle(DefaultAsyncResult.fail(result.cause()));
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
    if (succeed) {
      handler.handle(new DefaultFutureResult<>("Async result"));
    }
    else {
      handler.handle(new DefaultFutureResult<>(new Exception("Fail")));
    }
  }

  private void doSomethingWithTheResults(List<String> results) {
    this.results = results;
  }

  public List<String> results() {
    return results;
  }
}
