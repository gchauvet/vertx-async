package org.simondean.vertx.async.unit.examples;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;
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
