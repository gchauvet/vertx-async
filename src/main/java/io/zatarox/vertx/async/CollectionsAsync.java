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
import org.javatuples.*;

public final class CollectionsAsync {

    private CollectionsAsync() {
    }

    /**
     * Applies the function `consumer` to each item in `iterable`, in parallel.
     * The `consumer` is called with an item from the list, and a callback when
     * it has finished. If the `consumer` passes an error to its `callback`, the
     * main `handler` (for the `each` function) is immediately called with the
     * error.
     *
     * Note, that since this function applies `consumer` to each item in
     * parallel, there is no guarantee that the consumer functions will complete
     * in order.
     *
     * @param <T> Define the manipulated type.
     * @param instance The Vertx instance to use.
     * @param iterable A collection to iterate over.
     * @param consumer A function to apply to each item in `iterable`. The
     * iteratee is passed a `consumer` which must be called once it has
     * completed. If no error has occurred, the `callback` should be run without
     * arguments or with an explicit `null` argument. The array index is not
     * passed to the consumer. If you need the index, use `eachOf`.
     * @param handler A callback which is called when all `consumer` functions
     * have finished, or an error occurs.
     */
    public static <T> void each(final Vertx instance, final Collection<T> iterable, final BiConsumer<T, Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        if (iterable.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed());
        } else {
            final ObjectWrapper<Boolean> failed = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(iterable.size());
            for (T item : iterable) {
                instance.runOnContext(aVoid -> consumer.accept(item, result -> {
                    counter.setObject(counter.getObject() - 1);
                    if (result.failed()) {
                        if (!failed.getObject()) {
                            handler.handle(DefaultAsyncResult.fail(result));
                            failed.setObject(true);
                        }
                    } else if (counter.getObject() == 0 && !failed.getObject()) {
                        handler.handle(DefaultAsyncResult.succeed());
                    }
                }));

                if (failed.getObject()) {
                    break;
                }
            }
        }
    }

    public static <K, V> void forEachOf(final Vertx instance, final Map<K, V> iterable, final BiConsumer<KeyValue<K, V>, Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        if (iterable.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed());
        } else {
            final ObjectWrapper<Boolean> failed = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(iterable.size());
            for (final Map.Entry<K, V> item : iterable.entrySet()) {
                instance.runOnContext(aVoid -> consumer.accept(new KeyValue<>(item.getKey(), item.getValue()), result -> {
                    counter.setObject(counter.getObject() - 1);
                    if (result.failed()) {
                        if (!failed.getObject()) {
                            handler.handle(DefaultAsyncResult.fail(result));
                            failed.setObject(true);
                        }
                    } else if (counter.getObject() == 0 && !failed.getObject()) {
                        handler.handle(DefaultAsyncResult.succeed());
                    }
                }));

                if (failed.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * Produces a new collection of values by mapping each value in `iterable`
     * through the `consumer` function. The `consumer` is called with an item
     * from `iterbale` and a callback for when it has finished processing. Each
     * of these callback takes 2 arguments: an `error`, and the transformed item
     * from `iterable`. If `consumer` passes an error to its callback, the main
     * `handler` (for the `map` function) is immediately called with the error.
     *
     * Note, that since this function applies the `consumer` to each item in
     * parallel, there is no guarantee that the `consumer` functions will
     * complete in order. However, the results array will be in the same order
     * as the original `iterable`.
     *
     * @param <I> Define input type.
     * @param <O> Define output type.
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param consumer A function to apply to each item in `iterable`. The
     * iteratee is passed a `handler` which must be called once it has completed
     * with an error and a transformed item. Invoked with (item, callback).
     * @param handler A callback which is called when all `consumer` functions
     * have finished, or an error occurs. Results is a List of the transformed
     * items from the `iterable`.
     */
    public static <I, O> void map(final Vertx instance, final List<I> iterable, final BiConsumer<I, Handler<AsyncResult<O>>> consumer, final Handler<AsyncResult<Collection<O>>> handler) {
        final List<O> mapped = new ArrayList<>(iterable.size());
        if (iterable.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(mapped));
        } else {
            final ObjectWrapper<Boolean> failed = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(iterable.size());

            for (int i = 0; i < iterable.size(); i++) {
                final I item = iterable.get(i);
                final int pos = i;
                instance.runOnContext(aVoid -> consumer.accept(item, result -> {
                    counter.setObject(counter.getObject() - 1);
                    if (result.failed()) {
                        if (!failed.getObject()) {
                            handler.handle(DefaultAsyncResult.fail(result));
                            failed.setObject(true);
                        }
                    } else {
                        mapped.add(pos, result.result());
                        if (counter.getObject() == 0 && !failed.getObject()) {
                            handler.handle(DefaultAsyncResult.succeed(mapped));
                        }
                    }
                }));

                if (failed.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * Returns a new array of all the values in `iterable` which pass an async
     * truth test. This operation is performed in parallel, but the results
     * array will be in the same order as the original.
     *
     * @param <T> Define the manipulated type.
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param consumer A truth test to apply to each item in `iterable`. The
     * `consumer` is passed a `handler`, which must be called with a boolean
     * argument once it has completed.
     * @param handler A callback which is called after all the `iteratee`
     * functions have finished.
     */
    public static <T> void filter(final Vertx instance, final Collection<T> iterable, final BiConsumer<T, Handler<AsyncResult<Boolean>>> consumer, final Handler<AsyncResult<Collection<T>>> handler) {
        final List<T> filtered = new LinkedList<>();
        if (iterable.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(filtered));
        } else {
            final ObjectWrapper<Boolean> failed = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(iterable.size());
            for (T item : iterable) {
                instance.runOnContext(aVoid -> consumer.accept(item, result -> {
                    counter.setObject(counter.getObject() - 1);
                    if (result.failed()) {
                        if (!failed.getObject()) {
                            handler.handle(DefaultAsyncResult.fail(result));
                            failed.setObject(true);
                        }
                    } else {
                        if (result.result()) {
                            filtered.add(item);
                        }
                        if (counter.getObject() == 0 && !failed.getObject()) {
                            handler.handle(DefaultAsyncResult.succeed(filtered));
                        }
                    }
                }));

                if (failed.getObject()) {
                    break;
                }
            }
        }
    }

    public static <T> void reject(final Vertx instance, final Collection<T> iterable, final BiConsumer<T, Handler<AsyncResult<Boolean>>> consumer, final Handler<AsyncResult<Collection<T>>> handler) {
        filter(instance, iterable, (T t, Handler<AsyncResult<Boolean>> u) -> {
            consumer.accept(t, (Handler<AsyncResult<Boolean>>) (AsyncResult<Boolean> event) -> {
                if (event.succeeded()) {
                    u.handle(DefaultAsyncResult.succeed(!event.result()));
                } else {
                    u.handle(event);
                }
            });
        }, handler);
    }

    public static <I, O> void transform(final Vertx instance, final Collection<I> iterable, final BiConsumer<I, Handler<AsyncResult<O>>> consumer, final Handler<AsyncResult<Collection<O>>> handler) {
        final Iterator<I> iterator = iterable.iterator();
        final List<O> results = new ArrayList<>(iterable.size());

        instance.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                if (!iterator.hasNext()) {
                    handler.handle(DefaultAsyncResult.succeed(results));
                } else {
                    consumer.accept(iterator.next(), (Handler<AsyncResult<O>>) (AsyncResult<O> event1) -> {
                        if (event1.succeeded()) {
                            results.add(event1.result());
                            instance.runOnContext(this);
                        } else {
                            handler.handle(DefaultAsyncResult.fail(event1));
                        }
                    });
                }
            }
        });
    }

    public static <K, V, T, R> void transform(final Vertx instance, final Map<K, V> iterable, final BiConsumer<KeyValue<K, V>, Handler<AsyncResult<KeyValue<T, R>>>> consumer, final Handler<AsyncResult<Map<T, R>>> handler) {
        final Iterator<Map.Entry<K, V>> iterator = iterable.entrySet().iterator();
        final Map<T, R> results = new HashMap<>(iterable.size());

        instance.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                if (!iterator.hasNext()) {
                    handler.handle(DefaultAsyncResult.succeed(results));
                } else {
                    final Map.Entry<K, V> item = iterator.next();
                    consumer.accept(new KeyValue<>(item.getKey(), item.getValue()), (Handler<AsyncResult<KeyValue<T, R>>>) (AsyncResult<KeyValue<T, R>> event1) -> {
                        if (event1.succeeded()) {
                            results.put(event1.result().getKey(), event1.result().getValue());
                            instance.runOnContext(this);
                        } else {
                            handler.handle(DefaultAsyncResult.fail(event1));
                        }
                    });
                }
            }
        });
    }
    
    public static <I, O> void reduce(final Vertx instance, final Collection<I> collection, final O memo, final BiConsumer<Pair<I, O>, Handler<AsyncResult<O>>> function, final Handler<AsyncResult<O>> handler) {
        final Iterator<I> iterator = collection.iterator();
        final ObjectWrapper<O> value = new ObjectWrapper<>(memo);
        instance.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                if (!iterator.hasNext()) {
                    handler.handle(DefaultAsyncResult.succeed(value.getObject()));
                } else {
                    function.accept(new Pair<>(iterator.next(), value.getObject()), (Handler<AsyncResult<O>>) (AsyncResult<O> event1) -> {
                        if (event1.failed()) {
                            handler.handle(DefaultAsyncResult.fail(event1));
                        } else {
                            value.setObject(event1.result());
                            instance.runOnContext((Void) -> {
                                instance.runOnContext(this);
                            });
                        }
                    });
                }
            }
        });
    }

}
