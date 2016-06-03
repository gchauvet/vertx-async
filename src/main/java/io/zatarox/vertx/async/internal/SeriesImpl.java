package io.zatarox.vertx.async.internal;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import io.zatarox.vertx.async.DefaultAsyncResult;
import io.zatarox.vertx.async.Series;

public class SeriesImpl<T> implements Series<T> {

    private ArrayList<Consumer<Handler<AsyncResult<T>>>> tasks = new ArrayList<>();

    @Override
    public Series<T> task(Consumer<Handler<AsyncResult<T>>> task) {
        tasks.add(task);
        return this;
    }

    @Override
    public void run(Handler<AsyncResult<List<T>>> handler) {
        Iterator<Consumer<Handler<AsyncResult<T>>>> iterator = tasks.iterator();
        List<T> results = new ArrayList<>();

        FunctionWrapper<Runnable> visitor = new FunctionWrapper<>();
        visitor.wrap(() -> {
            if (!iterator.hasNext()) {
                handler.handle(DefaultAsyncResult.succeed(results));
                return;
            }

            Consumer<Handler<AsyncResult<T>>> task = iterator.next();

            Handler<AsyncResult<T>> taskHandler = (result) -> {
                if (result.failed()) {
                    handler.handle(DefaultAsyncResult.fail(result));
                    return;
                }

                results.add(result.result());
                visitor.f().run();
            };

            task.accept(taskHandler);
        });

        visitor.f().run();
    }
}
