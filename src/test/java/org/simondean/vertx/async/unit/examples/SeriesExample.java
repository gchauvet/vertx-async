package org.simondean.vertx.async.unit.examples;

import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.Series;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

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
        taskHandler.handle((AsyncResult<String>) new DefaultFutureResult(result));
      })
      .task(taskHandler -> {
        someAsyncMethodThatTakesAHandler(taskHandler);
      })
      .run(result -> {
        if (result.failed()) {
          handler.handle(new DefaultFutureResult(result.cause()));
          return;
        }

        List<String> resultList = result.result();
        doSomethingWithTheResults(resultList);

        handler.handle(new DefaultFutureResult(resultList));
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
