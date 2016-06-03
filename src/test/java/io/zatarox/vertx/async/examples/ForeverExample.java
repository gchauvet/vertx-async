package io.zatarox.vertx.async.examples;

import io.vertx.core.AsyncResult;
import io.zatarox.vertx.async.Async;
import io.zatarox.vertx.async.DefaultAsyncResult;
import io.vertx.core.Handler;

public class ForeverExample extends BaseExample {

    public void foreverExample(Handler<AsyncResult<String>> handler) {
        Async.forever()
                .task(taskHandler -> {
                    someAsyncMethodThatTakesAHandler(taskHandler);
                })
                .run(vertx, result -> {
                    handler.handle(DefaultAsyncResult.fail(result));
                });
    }

    private void someAsyncMethodThatTakesAHandler(Handler<AsyncResult<Void>> handler) {
        handler.handle(DefaultAsyncResult.fail(new Exception("Fail")));
    }
}
