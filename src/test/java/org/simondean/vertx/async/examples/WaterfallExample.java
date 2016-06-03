package org.simondean.vertx.async.examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;

public class WaterfallExample extends BaseExample {

    private final boolean succeed;
    private Integer result;

    public WaterfallExample(boolean succeed) {
        this.succeed = succeed;
    }

    public void waterfallExample(Handler<AsyncResult<Integer>> handler) {
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

    private void someAsyncMethodThatTakesAResultAndHandler(String result, Handler<AsyncResult<Integer>> handler) {
        if (!succeed) {
            handler.handle(DefaultAsyncResult.fail(new Exception("Fail")));
            return;
        }

        handler.handle(DefaultAsyncResult.succeed(Integer.parseInt(result)));
    }

    private void doSomethingWithTheResults(Integer result) {
        this.result = result;
    }

    public Integer result() {
        return result;
    }
}
