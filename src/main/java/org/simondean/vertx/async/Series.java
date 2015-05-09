package org.simondean.vertx.async;


import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.impl.DefaultFutureResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class Series<T> {
  private ArrayList<Consumer<AsyncResultHandler<T>>> tasks = new ArrayList<>();

  public Series<T> task(Consumer<AsyncResultHandler<T>> task) {
    tasks.add(task);
    return this;
  }

  public void run(AsyncResultHandler<List<T>> handler) {
    Iterator<Consumer<AsyncResultHandler<T>>> iterator = tasks.iterator();
    List<T> results = new ArrayList<>();

    FunctionWrapper<Runnable> visitor = new FunctionWrapper<>();
    visitor.wrap(() -> {
      if (!iterator.hasNext()) {
        handler.handle(new DefaultFutureResult(results));
        return;
      }

      Consumer<AsyncResultHandler<T>> task = iterator.next();

      AsyncResultHandler<T> taskHandler = (result) -> {
        if (result.failed()) {
          handler.handle(new DefaultFutureResult(result.cause()));
          return;
        }

        results.add(result.result());
        visitor.f().run();
      };

      try {
        task.accept(taskHandler);
      }
      catch (Exception e) {
        handler.handle(new DefaultFutureResult(e));
      }
    });

    visitor.f().run();
  }
}
