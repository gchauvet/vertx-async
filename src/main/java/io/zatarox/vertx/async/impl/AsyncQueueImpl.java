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
package io.zatarox.vertx.async.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import io.zatarox.vertx.async.AsyncQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.javatuples.Pair;

public final class AsyncQueueImpl<T> implements Handler<Void>, AsyncQueue<T> {

    private final BiConsumer<T, Handler<AsyncResult<Void>>> worker;
    private final Queue<Pair<T, Handler<AsyncResult<Void>>>> tasks = new ConcurrentLinkedQueue();
    private final Set<AsyncQueueListener> listeners = new ConcurrentHashSet();
    private final AtomicInteger concurrency = new AtomicInteger(0);
    private final AtomicInteger current = new AtomicInteger(0);

    public AsyncQueueImpl(final BiConsumer<T, Handler<AsyncResult<Void>>> worker) {
        this(worker, 5);
    }

    public AsyncQueueImpl(final BiConsumer<T, Handler<AsyncResult<Void>>> worker, final int concurrency) {
        this.worker = worker;
        setConcurrency(concurrency);
    }

    public int getConcurrency() {
        return concurrency.get();
    }

    public void setConcurrency(int concurrency) {
        if (concurrency < 1) {
            throw new IllegalArgumentException("Must be positive");
        }
        this.concurrency.set(concurrency);
    }

    /**
     * @return Number of running workers
     */
    public int getRunning() {
        return current.get();
    }

    public boolean add(final T task, final Handler<AsyncResult<Void>> handler) {
        return tasks.add(new Pair(task, handler));
    }

    public boolean add(final AsyncQueueListener listener) {
        return listeners.add(listener);
    }

    public boolean remove(final AsyncQueueListener listener) {
        return listeners.remove(listener);
    }

    public void handle(Void event) {
        if (tasks.isEmpty()) {
            fireEmptyPool();
        } else if (current.get() < concurrency.get()) {
            final Pair<T, Handler<AsyncResult<Void>>> task = tasks.poll();
            current.incrementAndGet();
            Vertx.currentContext().runOnContext(event1 -> {
                worker.accept(task.getValue0(), event2 -> {
                    task.getValue1().handle(event2);
                    current.decrementAndGet();
                    this.handle(event);
                });
            });
            this.handle(event);
        }
    }

    private void fireEmptyPool() {
        listeners.stream().forEach(listener -> {
            listener.poolEmpty(this);
        });
    }
}
