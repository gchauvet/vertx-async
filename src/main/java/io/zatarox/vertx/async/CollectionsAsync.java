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
import java.util.stream.Stream;
import org.javatuples.*;

public final class CollectionsAsync {

    private CollectionsAsync() {
    }

    /**
     * Applies the function {@code consumer} to each item in {@code iterable},
     * in parallel. The {@code consumer} is called with an item from the list,
     * and a callback when it has finished. If the {@code consumer} passes an
     * error to its {@code callback}, the main {@code handler} (for the
     * {@code each} function) is immediately called with the error.
     *
     * Note, that since this function applies {@code consumer} to each item in
     * parallel, there is no guarantee that the consumer functions will complete
     * in order.
     *
     * @param <T> Define the manipulated type.
     * @param instance The Vertx instance to use.
     * @param iterable A collection to iterate over.
     * @param consumer A function to apply to each item in {@code iterable}. The
     * iteratee is passed a {@code consumer} which must be called once it has
     * completed. If no error has occurred, the {@code callback} should be run
     * without arguments or with an explicit {@code null} argument. The array
     * index is not passed to the consumer. If you need the index, use
     * {@code eachOf}.
     * @param handler A callback which is called when all {@code consumer}
     * functions have finished, or an error occurs.
     */
    public static <T> void each(final Vertx instance, final Collection<T> iterable, final BiConsumer<T, Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        if (iterable.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed());
        } else {
            final ObjectWrapper<Boolean> stop = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(iterable.size());
            for (T item : iterable) {
                instance.runOnContext(aVoid -> consumer.accept(item, result -> {
                    counter.setObject(counter.getObject() - 1);
                    if (result.failed()) {
                        if (!stop.getObject()) {
                            stop.setObject(true);
                            handler.handle(DefaultAsyncResult.fail(result));
                        }
                    } else if (counter.getObject() == 0 && !stop.getObject()) {
                        handler.handle(DefaultAsyncResult.succeed());
                    }
                }));

                if (stop.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * Like {@code each}, except that it passes the tuple key/value as argument
     * to the consumer.
     *
     * @param <K> Define type of key.
     * @param <V> Define type of value.
     * @param instance The Vertx instance to use.
     * @param iterable A collection to iterate over.
     * @param consumer A function to apply to each item in {@code iterable}. The
     * {@code key} is the item's key. The iteratee is passed a {@code handler}
     * which must be called once it has completed. If no error has occurred, the
     * callback should be run without arguments or with an explicit {@code null}
     * argument.
     * @param handler A callback which is called when all {@code consumer}
     * functions have finished, or an error occurs.
     */
    public static <K, V> void each(final Vertx instance, final Map<K, V> iterable, final BiConsumer<KeyValue<K, V>, Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        if (iterable.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed());
        } else {
            final ObjectWrapper<Boolean> stop = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(iterable.size());
            for (final Map.Entry<K, V> item : iterable.entrySet()) {
                instance.runOnContext(aVoid -> consumer.accept(new KeyValue<>(item.getKey(), item.getValue()), result -> {
                    counter.setObject(counter.getObject() - 1);
                    if (result.failed()) {
                        if (!stop.getObject()) {
                            stop.setObject(true);
                            handler.handle(DefaultAsyncResult.fail(result));
                        }
                    } else if (counter.getObject() == 0 && !stop.getObject()) {
                        handler.handle(DefaultAsyncResult.succeed());
                    }
                }));

                if (stop.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * Produces a new collection of values by mapping each value in
     * {@code iterable} through the {@code consumer} function. The
     * {@code consumer} is called with an item from {@code iterbale} and a
     * callback for when it has finished processing. Each of these callback
     * takes 2 arguments: an {@code error}, and the transformed item from
     * {@code iterable}. If {@code consumer} passes an error to its callback,
     * the main {@code handler} (for the {@code map} function) is immediately
     * called with the error.
     *
     * Note, that since this function applies the {@code consumer} to each item
     * in parallel, there is no guarantee that the {@code consumer} functions
     * will complete in order. However, the results array will be in the same
     * order as the original {@code iterable}.
     *
     * @param <I> Define input type.
     * @param <O> Define output type.
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param consumer A function to apply to each item in {@code iterable}. The
     * iteratee is passed a {@code handler} which must be called once it has
     * completed with an error and a transformed item. Invoked with (item,
     * callback).
     * @param handler A callback which is called when all {@code consumer}
     * functions have finished, or an error occurs. Results is a List of the
     * transformed items from the {@code iterable}.
     */
    public static <I, O> void map(final Vertx instance, final List<I> iterable, final BiConsumer<I, Handler<AsyncResult<O>>> consumer, final Handler<AsyncResult<Collection<O>>> handler) {
        final List<O> mapped = new ArrayList<>(iterable.size());
        if (iterable.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(mapped));
        } else {
            final ObjectWrapper<Boolean> stop = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(iterable.size());

            for (int i = 0; i < iterable.size(); i++) {
                final I item = iterable.get(i);
                final int pos = i;
                instance.runOnContext(aVoid -> consumer.accept(item, result -> {
                    counter.setObject(counter.getObject() - 1);
                    if (result.failed()) {
                        if (!stop.getObject()) {
                            stop.setObject(true);
                            handler.handle(DefaultAsyncResult.fail(result));
                        }
                    } else {
                        mapped.add(pos, result.result());
                        if (counter.getObject() == 0 && !stop.getObject()) {
                            handler.handle(DefaultAsyncResult.succeed(mapped));
                        }
                    }
                }));

                if (stop.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * Returns a new collection of all the values in {@code iterable} which pass
     * an async truth test. This operation is performed in parallel, but the
     * results array will be in the same order as the original.
     *
     * @param <T> Define the manipulated type.
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param consumer A truth test to apply to each item in {@code iterable}.
     * The {@code consumer} is passed a {@code handler}, which must be called
     * with a boolean argument once it has completed.
     * @param handler A callback which is called after all the {@code consumer}
     * functions have finished.
     */
    public static <T> void filter(final Vertx instance, final Collection<T> iterable, final BiConsumer<T, Handler<AsyncResult<Boolean>>> consumer, final Handler<AsyncResult<Collection<T>>> handler) {
        final List<T> filtered = new LinkedList<>();
        if (iterable.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(filtered));
        } else {
            final ObjectWrapper<Boolean> stop = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(iterable.size());
            for (T item : iterable) {
                instance.runOnContext(aVoid -> consumer.accept(item, result -> {
                    counter.setObject(counter.getObject() - 1);
                    if (result.failed()) {
                        if (!stop.getObject()) {
                            stop.setObject(true);
                            handler.handle(DefaultAsyncResult.fail(result));
                        }
                    } else {
                        if (result.result()) {
                            filtered.add(item);
                        }
                        if (counter.getObject() == 0 && !stop.getObject()) {
                            handler.handle(DefaultAsyncResult.succeed(filtered));
                        }
                    }
                }));

                if (stop.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * The opposite of {@code filter}. Removes values that pass an {@code async}
     * truth test.
     *
     * @param <T> Define the manipulated type.
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param consumer A falsy test to apply to each item in {@code iterable}.
     * The {@code consumer} is passed a {@code handler}, which must be called
     * with a boolean argument once it has completed.
     * @param handler A callback which is called after all the {@code consumer}
     * functions have finished.
     */
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

    /**
     * A relative of {@code reduce}. Takes a Collection, and iterates over each
     * element in series, each step potentially mutating an {@code accumulator}
     * value. The type of the accumulator defaults to the type of collection
     * passed in.
     *
     * @param <I> Define the type of input data
     * @param <O> Define the type of output data
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param consumer A function applied to each item in the collection that
     * potentially modifies the accumulator. The {@code consumer} is passed a
     * {@code handler}. If an error is passed to the callback, the transform is
     * stopped and the main {@code handler} is immediately called with the
     * error.
     * @param handler A callback which is called after all the {@code consumer}
     * functions have finished. Result is the transformed accumulator.
     */
    public static <I, O> void transform(final Vertx instance, final Collection<I> iterable, final BiConsumer<I, Handler<AsyncResult<O>>> consumer, final Handler<AsyncResult<Collection<O>>> handler) {
        final Iterator<I> iterator = iterable.iterator();
        final List<O> result = new ArrayList<>(iterable.size());

        instance.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                if (!iterator.hasNext()) {
                    handler.handle(DefaultAsyncResult.succeed(result));
                } else {
                    consumer.accept(iterator.next(), (Handler<AsyncResult<O>>) (AsyncResult<O> event1) -> {
                        if (event1.succeeded()) {
                            result.add(event1.result());
                            instance.runOnContext(this);
                        } else {
                            handler.handle(DefaultAsyncResult.fail(event1));
                        }
                    });
                }
            }
        });
    }

    /**
     * A relative of {@code reduce}. Takes a Map, and iterates over each element
     * in series, each step potentially mutating an {@code accumulator} value.
     * The type of the accumulator defaults to the type of collection passed in.
     *
     * @param <K> Define the type of input key.
     * @param <V> Define the type of input value.
     * @param <T> Define the type of output key.
     * @param <R> Define the type of output value.
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param consumer A function applied to each item in the collection that
     * potentially modifies the accumulator. The {@code consumer} is passed a
     * {@code handler}. If an error is passed to the callback, the transform is
     * stopped and the main {@code handler} is immediately called with the
     * error.
     * @param handler A callback which is called after all the {@code consumer}
     * functions have finished. Result is the transformed accumulator.
     */
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

    /**
     * Reduces {@code collection} into a single value using an async
     * {@code consumer} to return each successive step. {@code memo} is the
     * initial state of the reduction. This function only operates in series.
     *
     * This function is for situations where each step in the reduction needs to
     * be async; if you can get the data before reducing it, then it's probably
     * a good idea to do so.
     *
     * @param <I> Define the type of input data
     * @param <O> Define the type of output data
     * @param instance Define Vertx instance.
     * @param collection A collection to iterate over.
     * @param memo Initial state of the reduction.
     * @param function A function applied to each item in the array to produce
     * the next step in the reduction. The {@code function} is passed a
     * {@code handler} which accepts an optional error as its first argument,
     * and the state of the reduction as the second. If an error is passed to
     * the callback, the reduction is stopped and the main {@code handler} is
     * immediately called.
     * @param handler
     */
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

    /**
     * Returns the first value in {@code collection} that passes an async truth
     * test. The {@code function} is applied in parallel, meaning the first
     * iteratee to return {@code true} will fire the detect {@code callback}
     * with that result. That means the result might not be the first item in
     * the original {@code collection} (in terms of order) that passes the test.
     *
     * @param <T> Define the manipulated type.
     * @param instance Define Vertx instance.
     * @param collection A collection to iterate over.
     * @param function A truth test to apply to each item in {@code collection}.
     * The iteratee is passed a {@code callback} which must be called with a
     * boolean argument once it has completed.
     * @param handler A callback which is called as soon as any iteratee returns
     * {@code true}, or after all the {@code function} functions have finished.
     * Result will be the first item in the array that passes the truth test
     * (function) or the value {@code null} if none passed.
     */
    public static <T> void detect(final Vertx instance, final Collection<T> collection, final BiConsumer<T, Handler<AsyncResult<Boolean>>> function, final Handler<AsyncResult<T>> handler) {
        if (collection.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(null));
        } else {
            final ObjectWrapper<Boolean> stop = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(collection.size());
            for (T item : collection) {
                instance.runOnContext(aVoid -> function.accept(item, event -> {
                    counter.setObject(counter.getObject() - 1);
                    if (event.succeeded()) {
                        // Prevent Unhandled exception in Netty
                        if (null != event.result() && event.result()) {
                            if (!stop.getObject()) {
                                stop.setObject(true);
                                handler.handle(DefaultAsyncResult.succeed(item));
                            }
                        } else if (counter.getObject() == 0 && !stop.getObject()) {
                            handler.handle(DefaultAsyncResult.succeed(null));
                        }
                    } else {
                        stop.setObject(true);
                        handler.handle(DefaultAsyncResult.fail(event));
                    }
                }));

                if (stop.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * Returns {@code true} if at least one element in the {@code collection}
     * satisfies an async test. If any iteratee call returns {@code true}, the
     * main {@code callback} is immediately called.
     *
     * @param <T> Define the manipulated type.
     * @param instance Define Vertx instance.
     * @param collection A collection to iterate over.
     * @param function A truth test to apply to each item in the array in
     * parallel.
     * @param handler A callback which is called as soon as any iteratee returns
     * {@code true}, or after all the iteratee functions have finished. Result
     * will be either {@code true} or {@code false} depending on the values of
     * the async tests.
     */
    public static <T> void some(final Vertx instance, final Collection<T> collection, final BiConsumer<T, Handler<AsyncResult<Boolean>>> function, final Handler<AsyncResult<Boolean>> handler) {
        if (collection.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(false));
        } else {
            final ObjectWrapper<Boolean> stop = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(collection.size());
            for (T item : collection) {
                instance.runOnContext(aVoid -> function.accept(item, event -> {
                    counter.setObject(counter.getObject() - 1);
                    if (event.succeeded()) {
                        // Prevent Unhandled exception in Netty
                        if (null != event.result() && event.result()) {
                            if (!stop.getObject()) {
                                stop.setObject(true);
                                handler.handle(DefaultAsyncResult.succeed(true));
                            }
                        } else if (counter.getObject() == 0 && !stop.getObject()) {
                            handler.handle(DefaultAsyncResult.succeed(false));
                        }
                    } else {
                        stop.setObject(true);
                        handler.handle(DefaultAsyncResult.fail(event));
                    }
                }));

                if (stop.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * Returns {@code true} if every element in {@code collection} satisfies an
     * async test. If any iteratee call returns {@code false}, the main
     * {@code callback} is immediately called.
     *
     * @param <T> Define the manipulated type.
     * @param instance Define Vertx instance.
     * @param collection A collection to iterate over.
     * @param function A truth test to apply to each item in the collection in
     * parallel.
     * @param handler A callback which is called after all the {code collection}
     * functions have finished. Result will be either {@code true} or
     * {@code false} depending on the values of the async tests.
     */
    public static <T> void every(final Vertx instance, final Collection<T> collection, final BiConsumer<T, Handler<AsyncResult<Boolean>>> function, final Handler<AsyncResult<Boolean>> handler) {
        if (collection.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(false));
        } else {
            final ObjectWrapper<Boolean> stop = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(collection.size());
            for (T item : collection) {
                instance.runOnContext(aVoid -> function.accept(item, event -> {
                    counter.setObject(counter.getObject() - 1);
                    if (event.succeeded()) {
                        // Prevent Unhandled exception in Netty
                        if (null != event.result() && !event.result()) {
                            if (!stop.getObject()) {
                                stop.setObject(true);
                                handler.handle(DefaultAsyncResult.succeed(false));
                            }
                        } else if (counter.getObject() == 0 && !stop.getObject()) {
                            handler.handle(DefaultAsyncResult.succeed(true));
                        }
                    } else {
                        stop.setObject(true);
                        handler.handle(DefaultAsyncResult.fail(event));
                    }
                }));

                if (stop.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * Applies {@code consumer} to each item in {@code collection},
     * concatenating the results. Returns the concatenated list. The
     * {@code iteratee}s are called in parallel, and the results are
     * concatenated as they return. There is no guarantee that the results array
     * will be returned in the original order of {@code collection} passed to
     * the {@code iteratee} function.
     *
     * @param <I> Define input type.
     * @param <O> Define output type.
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param consumer
     * @param handler
     */
    public static <I, O> void concat(final Vertx instance, final Collection<I> iterable, final BiConsumer<I, Handler<AsyncResult<Collection<O>>>> consumer, final Handler<AsyncResult<Collection<O>>> handler) {
        final List<O> results = new ArrayList<>(iterable.size());
        if (iterable.isEmpty()) {
            handler.handle(DefaultAsyncResult.succeed(results));
        } else {
            final ObjectWrapper<Boolean> stop = new ObjectWrapper<>(false);
            final ObjectWrapper<Integer> counter = new ObjectWrapper<>(iterable.size());
            for (I item : iterable) {
                instance.runOnContext(aVoid -> consumer.accept(item, result -> {
                    counter.setObject(counter.getObject() - 1);
                    if (result.failed()) {
                        if (!stop.getObject()) {
                            stop.setObject(true);
                            handler.handle(DefaultAsyncResult.fail(result));
                        }
                    } else {
                        if (result.result() != null) {
                            results.addAll(result.result());
                        }
                        if (counter.getObject() == 0 && !stop.getObject()) {
                            handler.handle(DefaultAsyncResult.succeed(results));
                        }
                    }
                }));

                if (stop.getObject()) {
                    break;
                }
            }
        }
    }

    /**
     * Sorts a list by the results of running each {@code collection} value
     * through the internal comparator.
     *
     * @param <T> Define the manipulated type.
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param handler A callback which is called after all the `iteratee`
     * functions have finished, or an error occurs. Results is the items from
     * the original {@code collection} sorted by the values returned by the
     * `iteratee` calls.
     */
    public static <T> void sort(final Vertx instance, final Collection<T> iterable, final Handler<AsyncResult<Collection<T>>> handler) {
        sort(instance, iterable, null, handler);
    }

    /**
     * Sorts a list by the results of running each {@code collection} value
     * through an async {@code comparator}.
     *
     * @param <T> Define the manipulated type.
     * @param instance Define Vertx instance.
     * @param iterable A collection to iterate over.
     * @param comparator A function used as comparator.
     * @param handler A callback which is called after all the `iteratee`
     * functions have finished, or an error occurs. Results is the items from
     * the original {@code collection} sorted by the values returned by the
     * `iteratee` calls.
     */
    public static <T> void sort(final Vertx instance, final Collection<T> iterable, final Comparator<T> comparator, final Handler<AsyncResult<Collection<T>>> handler) {
        instance.runOnContext((Void event) -> {
            Stream<T> stream = iterable.parallelStream();
            if (comparator != null) {
                stream = stream.sorted(comparator);
            } else {
                stream = stream.sorted();
            }
            handler.handle(DefaultAsyncResult.succeed(new ArrayList<>(Arrays.asList((T[]) stream.toArray()))));
        });
    }
}
