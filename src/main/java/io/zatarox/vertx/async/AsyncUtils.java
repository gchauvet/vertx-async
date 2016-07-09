/*
 * Copyright 2016 Guillaume Chauvet.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zatarox.vertx.async;

import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import io.zatarox.vertx.async.api.AsyncMemoize;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.zatarox.vertx.async.impl.AsyncMemoizeImpl;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class AsyncUtils {

    private AsyncUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    /**
     * Emulate a time limit on an asynchronous function. If the function does
     * not call its callback within the specified miliseconds, it will be called
     * with a timeout error.
     *
     * @param <T> Handled generic type
     * @param function A function which will be runned.
     * @param unit Time unit used for the delay time.
     * @param delay A time delay in the specified time unit.
     * @param handler An handler called when function finished or timeout is
     * reached
     */
    public static <T> void timeout(final Consumer<Handler<AsyncResult<T>>> function, final TimeUnit unit, final long delay, final Handler<AsyncResult<T>> handler) {
        Vertx.currentContext().executeBlocking(futur -> {
            final AtomicBoolean timeout = new AtomicBoolean(false);
            final long timerHandler = Vertx.currentContext().owner().setTimer(unit.toMillis(delay), id -> {
                timeout.set(true);
                futur.fail(new TimeoutException());
            });
            function.accept(event -> {
                Vertx.currentContext().owner().cancelTimer(timerHandler);
                if (!timeout.get()) {
                    handler.handle(event);
                }
            });
        }, handler);
    }

    /**
     * Caches the results of an async function. When creating a hash to store
     * function results against, the callback is omitted from the hash and an
     * optional hash function can be used.
     *
     * @param <I> Handled input generic type.
     * @param <O> Handled output generic type.
     * @param function The function to proxy and cache results from.
     * @return A proxy cache for the function.
     */
    public static <I, O> AsyncMemoize<I, O> memoize(final BiConsumer<I, Handler<AsyncResult<O>>> function) {
        return new AsyncMemoizeImpl(function);
    }

    /**
     * Returns a function that when called, calls-back with the values provided.
     * Useful as the first function in a {@code waterfall}.
     *
     * @param <T> Handled generic type.
     * @param value value of the "constant".
     * @return An handler wrapper of "constant" value.
     */
    public static <T> Consumer<Handler<AsyncResult<T>>> constant(final T value) {
        return handler -> {
            handler.handle(DefaultAsyncResult.succeed(value));
        };
    }

    /**
     * Take a sync function and make it async, passing its return value to a
     * callback. This is useful for plugging sync functions into a waterfall,
     * series, or other async functions. Any arguments passed to the generated
     * function will be passed to the wrapped function (except for the final
     * callback argument). Errors thrown will be passed to the callback.
     *
     * @param <I> Handled input generic type.
     * @param <O> Handled output generic type.
     * @param function The synchronous function to manage.
     * @return An asynchronous wrapper ready to be use with Vertx.
     */
    public static <I, O> BiConsumer<I, Handler<AsyncResult<O>>> asyncify(final Function<I, O> function) {
        return (item, handler) -> {
            try {
                handler.handle(DefaultAsyncResult.succeed(function.apply(item)));
            } catch (Throwable ex) {
                handler.handle(DefaultAsyncResult.fail(ex));
            }
        };
    }

}
