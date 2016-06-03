package org.simondean.vertx.async.internal;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.simondean.vertx.async.Forever;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.function.Consumer;

public class ForeverImpl implements Forever {

    private final Consumer<Handler<AsyncResult<Void>>> task;

    public ForeverImpl(Consumer<Handler<AsyncResult<Void>>> task) {
        this.task = task;
    }

    @Override
    public void run(Vertx vertx, Handler<AsyncResult<Void>> handler) {
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
