package org.simondean.vertx.async.internal;

import org.simondean.vertx.async.DefaultAsyncResult;
import org.simondean.vertx.async.Forever;
import org.simondean.vertx.async.FunctionWrapper;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Vertx;

import java.util.function.Consumer;

public class ForeverImpl implements Forever {
  private final Consumer<AsyncResultHandler<Void>> task;

  public ForeverImpl(Consumer<AsyncResultHandler<Void>> task) {
    this.task = task;
  }

  @Override
  public void run(Vertx vertx, AsyncResultHandler<Void> handler) {
    FunctionWrapper<Runnable> visitor = new FunctionWrapper<>();
    visitor.wrap(() -> task.accept(result -> {
      if (result.failed()) {
        handler.handle(DefaultAsyncResult.fail(result));
        return;
      }

      vertx.runOnContext(aVoid -> visitor.f().run());
    }));

    visitor.f().run();
  }
}
