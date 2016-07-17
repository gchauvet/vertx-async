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
package io.zatarox.vertx.async.impl;

import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import io.zatarox.vertx.async.api.AsyncWorker;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.zatarox.vertx.async.api.AsyncFlows;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class AsyncFlowsImpl implements AsyncFlows {

    private final Context context;

    public AsyncFlowsImpl(final Context context) {
        this.context = context;
    }

    @Override
    public <T> void series(final Collection<Consumer<Handler<AsyncResult<T>>>> tasks, final Handler<AsyncResult<List<T>>> handler) {
        context.runOnContext(new Handler<Void>() {
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
                            context.runOnContext((Void) -> {
                                context.runOnContext(this);
                            });
                        }
                    };
                    task.accept(taskHandler);
                }
            }
        });
    }

    @Override
    public <T> void retry(final AbstractRetryOptions options, final Consumer<Handler<AsyncResult<T>>> task, final Handler<AsyncResult<T>> handler) {
        context.runOnContext(options.build(task, handler));
    }

    @Override
    public <T> void forever(final Consumer<Handler<AsyncResult<T>>> task, final Handler<AsyncResult<T>> handler) {
        context.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                try {
                    task.accept((result) -> {
                        if (result.failed()) {
                            handler.handle(DefaultAsyncResult.fail(result));
                        } else {
                            context.runOnContext(this);
                        }
                    });
                } catch (Throwable ex) {
                    handler.handle(DefaultAsyncResult.fail(ex));
                }
            }
        });
    }

    @Override
    public <I, O> void waterfall(final Iterable<BiConsumer<I, Handler<AsyncResult<O>>>> tasks, final Handler<AsyncResult<?>> handler) {
        context.runOnContext(new Handler<Void>() {
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
                                context.runOnContext(this);
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

    @Override
    public <T> void parallel(final List<Consumer<Handler<AsyncResult<T>>>> tasks, final Handler<AsyncResult<List<T>>> handler) {
        final List<T> results = new ArrayList<>(tasks.size());
        if (tasks.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(results));
        } else {
            final AtomicBoolean stop = new AtomicBoolean(false);
            final AtomicInteger counter = new AtomicInteger(tasks.size());

            for (int i = 0; i < tasks.size(); i++) {
                final Consumer<Handler<AsyncResult<T>>> task = tasks.get(i);
                final int pos = i;
                context.runOnContext(aVoid -> {
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

    @Override
    public void whilst(final BooleanSupplier tester, final Consumer<Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        context.runOnContext(new Handler<Void>() {
            final AtomicBoolean stop = new AtomicBoolean(false);

            @Override
            public void handle(Void e) {
                try {
                    if (tester.getAsBoolean()) {
                        consumer.accept(e1 -> {
                            if (e1.succeeded()) {
                                context.runOnContext(this);
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

    @Override
    public void whilst(final Consumer<Handler<AsyncResult<Boolean>>> tester, final Consumer<Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        context.runOnContext(new Handler<Void>() {
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
                                            context.runOnContext(this);
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

    @Override
    public void until(final BooleanSupplier tester, final Consumer<Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        context.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void e) {
                try {
                    consumer.accept(e1 -> {
                        if (e1.succeeded()) {
                            if (tester.getAsBoolean()) {
                                context.runOnContext(this);
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

    @Override
    public <I, O> BiConsumer<I, Handler<AsyncResult<O>>> seq(final BiConsumer<I, Handler<AsyncResult<O>>>... functions) {
        return new BiConsumer<I, Handler<AsyncResult<O>>>() {
            private final Iterator<BiConsumer<I, Handler<AsyncResult<O>>>> iterator = Arrays.asList(functions).iterator();
            private final AtomicReference<BiConsumer<I, Handler<AsyncResult<O>>>> current = new AtomicReference(null);

            @Override
            public void accept(final I item, final Handler<AsyncResult<O>> handler) {
                if (iterator.hasNext()) {
                    current.set(iterator.next());
                    context.runOnContext(e1 -> {
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

    @Override
    public <T> void times(final int counter, final BiConsumer<Integer, Handler<AsyncResult<T>>> consumer, final Handler<AsyncResult<List<T>>> handler) {
        final List<T> mapped = new ArrayList<>(counter);
        if (counter < 1) {
            handler.handle(DefaultAsyncResult.succeed(mapped));
        } else {
            final AtomicBoolean stop = new AtomicBoolean(false);
            final AtomicInteger execution = new AtomicInteger(counter);

            for (int i = 0; i < counter; i++) {
                final int pos = i;
                context.runOnContext(aVoid -> {
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

    @Override
    public <T> void race(final List<Consumer<Handler<AsyncResult<T>>>> tasks, final Handler<AsyncResult<T>> handler) {
        if (tasks.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(null));
        } else {
            final AtomicBoolean stop = new AtomicBoolean(false);
            tasks.stream().forEach(task -> {
                context.runOnContext(event -> {
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

    @Override
    public <T> AsyncWorker createQueue(final BiConsumer<T, Handler<AsyncResult<Void>>> worker) {
        return new AsyncQueueImpl(worker);
    }

    @Override
    public <T> AsyncWorker createCargo(final BiConsumer<T, Handler<AsyncResult<Void>>> worker) {
        return new AsyncCargoImpl(worker);
    }

    @Override
    public <T> void each(final Collection<BiConsumer<T, Handler<AsyncResult<Void>>>> functions, final T args, final Handler<AsyncResult<Void>> handler) {
        if (functions.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed());
        } else {
            final AtomicBoolean stop = new AtomicBoolean(false);
            final AtomicInteger counter = new AtomicInteger(functions.size());

            functions.stream().forEach(function -> {
                context.runOnContext(event -> {
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
