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

import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import io.zatarox.vertx.async.impl.AbstractRetryOptions;
import io.zatarox.vertx.async.api.AsyncWorker;
import io.zatarox.vertx.async.impl.AsyncQueueImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.zatarox.vertx.async.impl.AsyncCargoImpl;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class AsyncFlows {

    private AsyncFlows() throws InstantiationException {
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
     * @param tasks A collection containing functions to run, each function is
     * passed a {@code handler} it must call on completion with an optional
     * error and an optional {@code result} value.
     * @param handler An optional handler to run once all the functions have
     * completed. This function gets a results array (or object) containing all
     * the result arguments passed to the {@code task} handlers.
     */
    public static <T> void series(Collection<Consumer<Handler<AsyncResult<T>>>> tasks, final Handler<AsyncResult<List<T>>> handler) {
        Vertx.currentContext().runOnContext(new Handler<Void>() {
            final Iterator<Consumer<Handler<AsyncResult<T>>>> iterator = tasks.iterator();
            final List<T> results = new ArrayList<>(tasks.size());

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
                            Vertx.currentContext().runOnContext((Void) -> {
                                Vertx.currentContext().runOnContext(this);
                            });
                        }
                    };
                    task.accept(taskHandler);
                }
            }
        });
    }

    /**
     * Attempts to get a successful response from {@code task} no more than
     * {@code times} times before returning an error. If the task is successful,
     * the {@code callback} will be passed the result of the successful task. If
     * all attempts fail, the callback will be passed the error and result (if
     * any) of the final attempt.
     *
     * @param <T> Define the manipulated data type.
     * @param task A function which receives two arguments: (1) a {@code task}
     * which must be called when finished, passing {@code err} (which can be
     * {@code null}) and the {@code result} of the function's execution, and (2)
     * a {@code results} object, containing the results of the previously
     * executed functions (if nested inside another control flow).
     * @param options Define options for retries
     * @param handler An optional callback which is called when the task has
     * succeeded, or after the final failed attempt. It receives the {@code err}
     * and {@code result} arguments of the last attempt at completing the
     * {@code task}.
     */
    public static <T> void retry(final AbstractRetryOptions options, final Consumer<Handler<AsyncResult<T>>> task, final Handler<AsyncResult<T>> handler) {
        Vertx.currentContext().runOnContext(options.build(task, handler));
    }

    /**
     * Calls the asynchronous function {@code task} with a callback parameter
     * that allows it to call itself again, in series, indefinitely. If an error
     * is passed to the callback then {@code handler} is called with the error,
     * and execution stops, otherwise it will never be called.
     *
     * @param <T> Define the manipulated data type.
     * @param task A function to call repeatedly.
     * @param handler when {@code task} passes an error to it's callback, this
     * function will be called, and execution stops.
     */
    public static <T> void forever(final Consumer<Handler<AsyncResult<T>>> task, final Handler<AsyncResult<T>> handler) {
        Vertx.currentContext().runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                try {
                    task.accept((result) -> {
                        if (result.failed()) {
                            handler.handle(DefaultAsyncResult.fail(result));
                        } else {
                            Vertx.currentContext().runOnContext(this);
                        }
                    });
                } catch (Throwable ex) {
                    handler.handle(DefaultAsyncResult.fail(ex));
                }
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
     * @param tasks An array of functions to run, each function is passed with
     * previously computed result thougth the {@code handler}.
     * @param handler Handler to run once all the functions have completed. This
     * will be passed the results of the last task's callback.
     */
    public static <I, O> void waterfall(final Iterable<BiConsumer<I, Handler<AsyncResult<O>>>> tasks, final Handler<AsyncResult<?>> handler) {
        Vertx.currentContext().runOnContext(new Handler<Void>() {
            private final Iterator<BiConsumer<I, Handler<AsyncResult<O>>>> iterator = tasks.iterator();
            private final AtomicBoolean stop = new AtomicBoolean();
            private I result = null;

            @Override
            public void handle(Void event) {
                if (iterator.hasNext()) {
                    try {
                        iterator.next().accept(result, event1 -> {
                            if (event1.succeeded()) {
                                result = (I) event1.result();
                                Vertx.currentContext().runOnContext(this);
                            } else {
                                stop.set(true);
                                handler.handle(DefaultAsyncResult.fail(event1));
                            }
                        });
                    } catch (Throwable ex) {
                        if (!stop.get()) {
                            stop.set(true);
                            handler.handle(DefaultAsyncResult.fail(ex));
                        }
                    }
                } else {
                    handler.handle(DefaultAsyncResult.succeed(result));
                }
            }
        });
    }

    /**
     * Run the {@code tasks} collection of functions in parallel, without
     * waiting until the previous function has completed. If any of the
     * functions pass an error to its callback, the main {@code handler} is
     * immediately called with the value of the error. Once the {@code tasks}
     * have completed, the results are passed to the final {@code handler} as an
     * array.
     *
     * **Note:** {@code parallel} is about kicking-off I/O tasks in parallel,
     * not about parallel execution of code. If your tasks do not use any timers
     * or perform any I/O, they will actually be executed in series. Any
     * synchronous setup sections for each task will happen one after the other.
     *
     * @param <T> Define the manipulated data type.
     * @param tasks Collection of tasks to run.
     * @param handler A callback to run once all the functions have completed
     * successfully. This function gets a results array (or object) containing
     * all the result arguments passed to the task callbacks.
     */
    public static <T> void parallel(List<Consumer<Handler<AsyncResult<T>>>> tasks, final Handler<AsyncResult<List<T>>> handler) {
        final List<T> results = new ArrayList<>(tasks.size());
        if (tasks.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(results));
        } else {
            final AtomicBoolean stop = new AtomicBoolean(false);
            final AtomicInteger counter = new AtomicInteger(tasks.size());

            for (int i = 0; i < tasks.size(); i++) {
                final Consumer<Handler<AsyncResult<T>>> task = tasks.get(i);
                final int pos = i;
                Vertx.currentContext().runOnContext(aVoid -> {
                    try {
                        task.accept(result -> {
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
                        });
                    } catch (Throwable ex) {
                        if (!stop.get()) {
                            stop.set(true);
                            handler.handle(DefaultAsyncResult.fail(ex));
                        }
                    }
                });
            }
        }
    }

    /**
     * Repeatedly call {@code consumer}, while {@code tester} returns
     * {@code true}. Calls {@code handler} when stopped, or an error occurs.
     *
     * @param tester A synchronous truth test to perform before each execution
     * of {@code consumer}.
     * @param consumer A function which is called each time {@code tester}
     * passes.
     * @param handler A callback which is called after the test function has
     * failed and repeated execution of {@code consumer} has stopped.
     */
    public static void whilst(final BooleanSupplier tester, Consumer<Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        Vertx.currentContext().runOnContext(new Handler<Void>() {
            final AtomicBoolean stop = new AtomicBoolean(false);

            @Override
            public void handle(Void e) {
                try {
                    if (tester.getAsBoolean()) {
                        consumer.accept(e1 -> {
                            if (e1.succeeded()) {
                                Vertx.currentContext().runOnContext(this);
                            } else {
                                stop.set(true);
                                handler.handle(DefaultAsyncResult.fail(e1));
                            }
                        });
                    } else if (!stop.get()) {
                        handler.handle(DefaultAsyncResult.succeed());
                    }
                } catch (Throwable ex) {
                    if (!stop.get()) {
                        stop.set(true);
                        handler.handle(DefaultAsyncResult.fail(ex));
                    }
                }
            }
        });
    }

    public static void whilst(final Consumer<Handler<AsyncResult<Boolean>>> tester, Consumer<Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        Vertx.currentContext().runOnContext(new Handler<Void>() {
            final AtomicBoolean stop = new AtomicBoolean(false);

            @Override
            public void handle(Void e) {
                try {
                    tester.accept(event -> {
                        if (event.succeeded()) {
                            if (event.result()) {
                                try {
                                    consumer.accept(e1 -> {
                                        if (e1.succeeded()) {
                                            Vertx.currentContext().runOnContext(this);
                                        } else {
                                            stop.set(true);
                                            handler.handle(DefaultAsyncResult.fail(e1));
                                        }
                                    });
                                } catch (Throwable ex) {
                                    if (!stop.get()) {
                                        stop.set(true);
                                        handler.handle(DefaultAsyncResult.fail(ex));
                                    }
                                }
                            } else {
                                stop.set(true);
                                handler.handle(DefaultAsyncResult.succeed());
                            }
                        } else if (!stop.get()) {
                            stop.set(true);
                            handler.handle(DefaultAsyncResult.fail(event));
                        }
                    });
                } catch (Throwable ex) {
                    if (!stop.get()) {
                        stop.set(true);
                        handler.handle(DefaultAsyncResult.fail(ex));
                    }
                }
            }
        });
    }

    /**
     * Repeatedly call {@code consumer} until {@code tester} returns
     * {@code false}. Calls {@code handler} when stopped, or an error occurs.
     *
     * @param tester synchronous truth test to perform after each execution of
     * {@code consumer}.
     * @param consumer A function which is called each time {@code tester}
     * passes.
     * @param handler A callback which is called after the test function has
     * failed and repeated execution of {@code consumer} has stopped.
     */
    public static void until(final BooleanSupplier tester, final Consumer<Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        Vertx.currentContext().runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void e) {
                try {
                    consumer.accept(e1 -> {
                        if (e1.succeeded()) {
                            if (tester.getAsBoolean()) {
                                Vertx.currentContext().runOnContext(this);
                            } else {
                                handler.handle(DefaultAsyncResult.succeed());
                            }
                        } else {
                            handler.handle(DefaultAsyncResult.fail(e1));
                        }
                    });
                } catch (Throwable ex) {
                    handler.handle(DefaultAsyncResult.fail(ex));
                }
            }
        });
    }

    /**
     * Creates a function which is a composition of the passed asynchronous
     * functions. Each function consumes the return value of the previous
     * function.
     *
     * @param <I> Define input data type of functions
     * @param <O> Define ouput data type of functions
     *
     * @param functions Asynchronous functions to seq
     * @return A proxy for asynchronous functions
     */
    public static <I, O> BiConsumer<I, Handler<AsyncResult<O>>> seq(final BiConsumer<I, Handler<AsyncResult<O>>>... functions) {
        return new BiConsumer<I, Handler<AsyncResult<O>>>() {
            private final Iterator<BiConsumer<I, Handler<AsyncResult<O>>>> iterator = Arrays.asList(functions).iterator();
            private final AtomicReference<BiConsumer<I, Handler<AsyncResult<O>>>> current = new AtomicReference(null);

            @Override
            public void accept(final I item, final Handler<AsyncResult<O>> handler) {
                if (iterator.hasNext()) {
                    current.set(iterator.next());
                    Vertx.currentContext().runOnContext(e1 -> {
                        try {
                            current.get().accept(item, e2 -> {
                                if (e2.succeeded()) {
                                    this.accept((I) e2.result(), handler);
                                } else {
                                    handler.handle(DefaultAsyncResult.fail(e2));
                                }
                            });
                        } catch (Throwable ex) {
                            handler.handle(DefaultAsyncResult.fail(ex));
                        }
                    });
                } else {
                    handler.handle(DefaultAsyncResult.succeed((O) item));
                }
            }
        };
    }

    /**
     * Calls the {@code consumer} function {@code counter} times, and
     * accumulates results in the same manner you would use with
     * {@code AsyncCollections.map}.
     *
     * @param <T> Define the manipulated type.
     * @param counter The number of times to run the function.
     * @param consumer The function to call {@code n} times. Invoked with the
     * iteration index and a callback.
     * @param handler A callback which is called after the test function has
     * failed and repeated execution of {@code consumer} has stopped.
     */
    public static <T> void times(final int counter, final BiConsumer<Integer, Handler<AsyncResult<T>>> consumer, final Handler<AsyncResult<List<T>>> handler) {
        final List<T> mapped = new ArrayList<>(counter);
        if (counter < 1) {
            handler.handle(DefaultAsyncResult.succeed(mapped));
        } else {
            final AtomicBoolean stop = new AtomicBoolean(false);
            final AtomicInteger execution = new AtomicInteger(counter);

            for (int i = 0; i < counter; i++) {
                final int pos = i;
                Vertx.currentContext().runOnContext(aVoid -> {
                    try {
                        consumer.accept(pos, result -> {
                            if (result.failed() || stop.get()) {
                                if (!stop.get()) {
                                    stop.set(true);
                                    handler.handle(DefaultAsyncResult.fail(result));
                                }
                            } else {
                                mapped.add(pos, result.result());
                                if (execution.decrementAndGet() < 1 && !stop.get()) {
                                    handler.handle(DefaultAsyncResult.succeed(mapped));
                                }
                            }
                        });
                    } catch (Throwable ex) {
                        stop.set(true);
                        handler.handle(DefaultAsyncResult.fail(ex));
                    }
                });
            }
        }
    }

    /**
     * Runs the {@code tasks} array of functions in parallel, without waiting
     * until the previous function has completed. Once any the {@code tasks}
     * completed or pass an error to its callback, the main {@code handler} is
     * immediately called.
     *
     * @param <T> Define the manipulated type.
     * @param tasks An array containing functions to run.
     * @param handler A callback to run once any of the functions have
     * completed. This function gets an error or result from the first function
     * that completed.
     */
    public static <T> void race(List<Consumer<Handler<AsyncResult<T>>>> tasks, final Handler<AsyncResult<T>> handler) {
        if (tasks.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(null));
        } else {
            final AtomicBoolean stop = new AtomicBoolean(false);
            tasks.stream().forEach(task -> {
                Vertx.currentContext().runOnContext(event -> {
                    try {
                        task.accept(result -> {
                            if (!stop.get()) {
                                stop.set(true);
                                handler.handle(result);
                            }
                        });
                    } catch (Throwable ex) {
                        if (!stop.get()) {
                            stop.set(true);
                            handler.handle(DefaultAsyncResult.fail(ex));
                        }
                    }
                });
            });
        }
    }

    /**
     * Creates a queue object with the specified concurrency. Tasks added to the
     * queue are processed in parallel (up to the concurrency limit). If all
     * workers are in progress, the task is queued until one becomes available.
     * Once a worker completes a task, that task's callback is called.
     *
     * @param <T> The manipulated type.
     * @param worker The worker used to process the queue
     * @return A queue of tasks for the worker function to complete.
     */
    public static <T> AsyncWorker createQueue(final BiConsumer<T, Handler<AsyncResult<Void>>> worker) {
        return new AsyncQueueImpl(worker);
    }

    /**
     * Creates a cargo object with the specified payload. Tasks added to the
     * cargo will be processed altogether (up to the payload limit). If the
     * worker is in progress, the task is queued until it becomes available.
     * Once the worker has completed some tasks, each callback of those tasks is
     * called. Check out these animations for how cargo and queue work.
     *
     * While queue passes only one task to one of a group of workers at a time,
     * cargo passes an array of tasks to a single worker, repeating when the
     * worker is finished.
     *
     * @param <T> The manipulated type.
     * @param worker The worker used to process tasks
     * @return A cargo for processing tasks through the provided worker
     * function.
     */
    public static <T> AsyncWorker createCargo(final BiConsumer<T, Handler<AsyncResult<Void>>> worker) {
        return new AsyncCargoImpl(worker);
    }

    /**
     * Applies the provided arguments to each function in the array, calling
     * {@code handler} after all functions have completed. If you only provide
     * the first argument, then it will return a function which lets you pass in
     * the arguments as if it were a single function call.
     *
     * @param <T> The manipulated type.
     * @param args An object representing arguments.
     * @param functions A collection of asynchronous functions to all call with
     * the same arguments.
     * @param handler The final argument should be the callback, called when all
     * functions have completed processing.
     */
    public static <T> void each(final Collection<BiConsumer<T, Handler<AsyncResult<Void>>>> functions, final T args, final Handler<AsyncResult<Void>> handler) {
        if (functions.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed());
        } else {
            final AtomicBoolean stop = new AtomicBoolean(false);
            final AtomicInteger counter = new AtomicInteger(functions.size());

            functions.stream().forEach(function -> {
                Vertx.currentContext().runOnContext(event -> {
                    try {
                        function.accept(args, result -> {
                            if (result.failed() || stop.get()) {
                                if (!stop.get()) {
                                    stop.set(true);
                                    handler.handle(DefaultAsyncResult.fail(result));
                                }
                            } else if (counter.decrementAndGet() == 0 && !stop.get()) {
                                handler.handle(DefaultAsyncResult.succeed());
                            }
                        });
                    } catch (Throwable ex) {
                        stop.set(true);
                        handler.handle(DefaultAsyncResult.fail(ex));
                    }
                });
            });
        }
    }
}
