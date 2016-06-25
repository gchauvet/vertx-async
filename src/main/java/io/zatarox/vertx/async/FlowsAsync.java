/*
 * Copyright 2004-2016 Guillaume Chauvet.
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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class FlowsAsync {

    private FlowsAsync() throws InstantiationException {
        throw new InstantiationException();
    }

    /**
     * Run the functions in the {@code tasks} collection in series, each one
     * running once the previous function has completed. If any functions in the
     * series pass an error to its callback, no more functions are run, and
     * {@code handler} is immediately called with the value of the error.
     * Otherwise, {@code handler} receives an array of results when
     * {@code tasks} have completed.
     *
     * It is also possible to use an object instead of an array. Each property
     * will be run as a function, and the results will be passed to the final
     * {@code callback} as an object instead of an array.
     *
     * @param <T> Define the manipulated data type.
     * @param instance Define Vertx instance.
     * @param tasks A collection containing functions to run, each function is
     * passed a {@code handler} it must call on completion with an optional
     * error and an optional {@code result} value.
     * @param handler An optional handler to run once all the functions have
     * completed. This function gets a results array (or object) containing all
     * the result arguments passed to the {@code task} handlers.
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
     * Attempts to get a successful response from {@code task} no more than
     * {@code times} times before returning an error. If the task is successful,
     * the {@code callback} will be passed the result of the successful task. If
     * all attempts fail, the callback will be passed the error and result (if
     * any) of the final attempt.
     *
     * @param <T> Define the manipulated data type.
     * @param instance Define Vertx instance.
     * @param task A function which receives two arguments: (1) a `callback(err,
     * result)` which must be called when finished, passing {@code err} (which
     * can be {@code null}) and the {@code result} of the function's execution,
     * and (2) a {@code results} object, containing the results of the
     * previously executed functions (if nested inside another control flow).
     * @param times The number of attempts to make before giving up.
     * @param handler An optional callback which is called when the task has
     * succeeded, or after the final failed attempt. It receives the {@code err}
     * and {@code result} arguments of the last attempt at completing the
     * {@code task}.
     */
    public static <T> void retry(final Vertx instance, final Consumer<Handler<AsyncResult<T>>> task, final long times, final Handler<AsyncResult<T>> handler) {
        instance.runOnContext((Void) -> {
            task.accept((Handler<AsyncResult<T>>) new Handler<AsyncResult<T>>() {
                private final AtomicInteger count = new AtomicInteger(0);

                @Override
                public void handle(AsyncResult<T> result) {
                    if (result.failed()) {
                        if (count.incrementAndGet() > times) {
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
     * Calls the asynchronous function {@code task} with a callback parameter
     * that allows it to call itself again, in series, indefinitely. If an error
     * is passed to the callback then {@code handler} is called with the error,
     * and execution stops, otherwise it will never be called.
     *
     * @param <T> Define the manipulated data type.
     * @param instance Define Vertx instance.
     * @param task A function to call repeatedly.
     * @param handler when {@code task} passes an error to it's callback, this
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
     * Runs the {@code tasks} array of functions in series, each passing their
     * results to the next in the array. However, if any of the {@code tasks}
     * pass an error to their own callback, the next function is not executed,
     * and the main {@code callback} is immediately called with the error.
     *
     * @param <I> Define input data type of functions
     * @param <O> Define ouput data type of functions
     * @param instance Define Vertx instance.
     * @param tasks An array of functions to run, each function is passed with
     * previously computed result thougth the {@code handler}.
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
                    iterator.next().accept(result, event1 -> {
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

    /**
     * Run the {@code tasks} collection of functions in parallel, without waiting
     * until the previous function has completed. If any of the functions pass
     * an error to its callback, the main {@code handler} is immediately called with
     * the value of the error. Once the {@code tasks} have completed, the results are
     * passed to the final {@code handler} as an array.
     *
     * **Note:** {@code parallel} is about kicking-off I/O tasks in parallel, not
     * about parallel execution of code. If your tasks do not use any timers or
     * perform any I/O, they will actually be executed in series. Any
     * synchronous setup sections for each task will happen one after the other.
     *
     * @param <T> Define the manipulated data type.
     * @param instance Define Vertx instance.
     * @param tasks Collection of tasks to run.
     * @param handler A callback to run once all the functions have completed
     * successfully. This function gets a results array (or object) containing
     * all the result arguments passed to the task callbacks.
     */
    public static <T> void parallel(final Vertx instance, List<Consumer<Handler<AsyncResult<T>>>> tasks, final Handler<AsyncResult<List<T>>> handler) {
        final List<T> results = new ArrayList<>(tasks.size());
        if (tasks.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(results));
        } else {
            instance.runOnContext(event -> {
                final AtomicBoolean stop = new AtomicBoolean(false);
                final AtomicInteger counter = new AtomicInteger(tasks.size());

                for (int i = 0; i < tasks.size(); i++) {
                    final Consumer<Handler<AsyncResult<T>>> task = tasks.get(i);
                    final int pos = i;
                    instance.runOnContext(aVoid -> task.accept(result -> {
                        if (result.failed() || stop.get()) {
                            if (!stop.get()) {
                                stop.set(true);
                                handler.handle(DefaultAsyncResult.fail(result));
                            }
                        } else {
                            results.add(pos, result.result());
                            if (counter.decrementAndGet() == 0 && !stop.get()) {
                                handler.handle(DefaultAsyncResult.succeed(results));
                            }
                        }
                    }));
                }
            });
        }
    }
}
