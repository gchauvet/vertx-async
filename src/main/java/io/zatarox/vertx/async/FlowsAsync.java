/*
 * The MIT License
 *
 * Copyright 2016 Guillaume Chauvet.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.zatarox.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class FlowsAsync {

    private FlowsAsync() {
    }

    /**
     * Run the functions in the `tasks` collection in series, each one running
     * once the previous function has completed. If any functions in the series
     * pass an error to its callback, no more functions are run, and `handler`
     * is immediately called with the value of the error. Otherwise, `handler`
     * receives an array of results when `tasks` have completed.
     *
     * It is also possible to use an object instead of an array. Each property
     * will be run as a function, and the results will be passed to the final
     * `callback` as an object instead of an array.
     *
     * @param <T> Define the manipulated data type.
     * @param instance Define Vertx instance.
     * @param tasks A collection containing functions to run, each function is
     * passed a `handler` it must call on completion with an optional error and
     * an optional `result` value.
     * @param handler An optional handler to run once all the functions have
     * completed. This function gets a results array (or object) containing all
     * the result arguments passed to the `task` handlers.
     */
    public static <T> void series(final Vertx instance, Collection<Consumer<Handler<AsyncResult<T>>>> tasks, final Handler<AsyncResult<List<T>>> handler) {
        final Iterator<Consumer<Handler<AsyncResult<T>>>> iterator = tasks.iterator();
        final List<T> results = new ArrayList<>(tasks.size());

        final Handler<Void> internal = new Handler<Void>() {
            @Override
            public void handle(Void event) {
                if (!iterator.hasNext()) {
                    handler.handle(DefaultAsyncResult.succeed(results));
                } else {
                    final Consumer<Handler<AsyncResult<T>>> task = iterator.next();

                    final Handler<AsyncResult<T>> taskHandler = (result) -> {
                        if (result.failed()) {
                            handler.handle(DefaultAsyncResult.fail(result));
                        } else {
                            results.add(result.result());
                            instance.runOnContext((Void) -> {
                                instance.runOnContext(this);
                            });
                        }
                    };
                    task.accept(taskHandler);
                }
            }
        };
        instance.runOnContext(internal);
    }

    /**
     * Attempts to get a successful response from `task` no more than `times`
     * times before returning an error. If the task is successful, the
     * `callback` will be passed the result of the successful task. If all
     * attempts fail, the callback will be passed the error and result (if any)
     * of the final attempt.
     *
     * @param <T> Define the manipulated data type.
     * @param instance Define Vertx instance.
     * @param task A function which receives two arguments: (1) a `callback(err,
     * result)` which must be called when finished, passing `err` (which can be
     * `null`) and the `result` of the function's execution, and (2) a `results`
     * object, containing the results of the previously executed functions (if
     * nested inside another control flow).
     * @param times The number of attempts to make before giving up.
     * @param handler An optional callback which is called when the task has
     * succeeded, or after the final failed attempt. It receives the `err` and
     * `result` arguments of the last attempt at completing the `task`.
     */
    public static <T> void retry(final Vertx instance, final Consumer<Handler<AsyncResult<T>>> task, final long times, final Handler<AsyncResult<T>> handler) {
        instance.runOnContext((Void) -> {
            task.accept((Handler<AsyncResult<T>>) new Handler<AsyncResult<T>>() {
                private final ObjectWrapper<Integer> count = new ObjectWrapper<>(0);

                @Override
                public void handle(AsyncResult<T> result) {
                    if (result.failed()) {
                        count.setObject(count.getObject() + 1);
                        if (count.getObject() > times) {
                            handler.handle(DefaultAsyncResult.fail(result));
                        } else {
                            instance.runOnContext((Void) -> {
                                task.accept(this);
                            });
                        }
                    } else {
                        handler.handle(DefaultAsyncResult.succeed(result.result()));
                    }
                }
            });
        });
    }

    /**
     * Calls the asynchronous function `task` with a callback parameter that
     * allows it to call itself again, in series, indefinitely. If an error is
     * passed to the callback then `handler` is called with the error, and
     * execution stops, otherwise it will never be called.
     *
     * @param <T> Define the manipulated data type.
     * @param instance Define Vertx instance.
     * @param task A function to call repeatedly.
     * @param handler when `task` passes an error to it's callback, this
     * function will be called, and execution stops.
     */
    public static <T> void forever(final Vertx instance, final Consumer<Handler<AsyncResult<T>>> task, final Handler<AsyncResult<T>> handler) {
        instance.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                task.accept((result) -> {
                    if (result.failed()) {
                        handler.handle(DefaultAsyncResult.fail(result));
                    } else {
                        instance.runOnContext(this);
                    }
                });
            }
        });
    }

    /**
     * Runs the `tasks` array of functions in series, each passing their results
     * to the next in the array. However, if any of the `tasks` pass an error to
     * their own callback, the next function is not executed, and the main
     * `callback` is immediately called with the error.
     *
     * @param <I> Define input data type of functions
     * @param <O> Define ouput data type of functions
     * @param instance Define Vertx instance.
     * @param tasks An array of functions to run, each function is passed with
     * previously computed result thougth the `handler`.
     * @param handler Handler to run once all the functions have completed. This
     * will be passed the results of the last task's callback.
     */
    public static <I, O> void waterfall(final Vertx instance, final Iterable<BiConsumer<I, Handler<AsyncResult<O>>>> tasks, final Handler<AsyncResult<?>> handler) {
        instance.runOnContext(new Handler<Void>() {
            private final Iterator<BiConsumer<I, Handler<AsyncResult<O>>>> iterator = tasks.iterator();
            private I result = null;

            @Override
            public void handle(Void event) {
                if (iterator.hasNext()) {
                    iterator.next().accept(result, (Handler<AsyncResult<O>>) (AsyncResult<O> event1) -> {
                        if (event1.succeeded()) {
                            result = (I) event1.result();
                            instance.runOnContext(this);
                        } else {
                            handler.handle(DefaultAsyncResult.fail(event1));
                        }
                    });
                } else {
                    handler.handle(DefaultAsyncResult.succeed(result));
                }
            }
        });
    }
}
