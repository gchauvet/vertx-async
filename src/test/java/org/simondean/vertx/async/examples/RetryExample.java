package org.simondean.vertx.async.examples;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;
import io.vertx.core.Handler;

public class RetryExample extends BaseExample {

    private final boolean succeed;
    private String result;

    public RetryExample(boolean succeed) {
        this.succeed = succeed;
    }

    public void retryExample(Handler<AsyncResult<String>> handler) {
        Async.retry()
                .<String>task(taskHandler -> {
                    someAsyncMethodThatTakesAHandler(taskHandler);
                })
                .times(5)
                .run(result -> {
                    if (result.failed()) {
                        handler.handle(DefaultAsyncResult.fail(result));
                        return;
                    }

                    String resultValue = result.result();

                    doSomethingWithTheResults(resultValue);

                    handler.handle(DefaultAsyncResult.succeed(resultValue));
                });
    }

    private void someAsyncMethodThatTakesAHandler(Handler<AsyncResult<String>> handler) {
        if (!succeed) {
            handler.handle(DefaultAsyncResult.fail(new Exception("Fail")));
            return;
        }

        handler.handle(DefaultAsyncResult.succeed("Async result"));
    }

    private void doSomethingWithTheResults(String result) {
        this.result = result;
    }

    public String result() {
        return result;
    }
}
