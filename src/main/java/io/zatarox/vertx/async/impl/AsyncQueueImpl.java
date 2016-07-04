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
import java.util.function.Consumer;
import org.javatuples.Pair;

public final class AsyncQueueImpl implements Handler<Void>, AsyncQueue {

    private final Queue<Pair<Consumer<Handler<AsyncResult<Void>>>, Handler<AsyncResult<Void>>>> workers = new ConcurrentLinkedQueue();
    private final Set<WorkersQueueListener> listeners = new ConcurrentHashSet();
    private final AtomicInteger concurrency = new AtomicInteger(0);
    private final AtomicInteger current = new AtomicInteger(0);

    public AsyncQueueImpl() {
        this.concurrency.set(5);
    }

    public AsyncQueueImpl(int concurrency) {
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

    public boolean add(final Consumer<Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        return workers.add(new Pair(consumer, handler));
    }

    public boolean add(final WorkersQueueListener listener) {
        return listeners.add(listener);
    }

    public boolean remove(final WorkersQueueListener listener) {
        return listeners.remove(listener);
    }

    public void handle(Void event) {
        if (workers.isEmpty()) {
            fireEmptyPool();
        } else if (current.get() < concurrency.get()) {
            final Pair<Consumer<Handler<AsyncResult<Void>>>, Handler<AsyncResult<Void>>> worker = workers.poll();
            current.incrementAndGet();
            Vertx.currentContext().runOnContext(event1 -> {
                worker.getValue0().accept(event2 -> {
                    current.decrementAndGet();
                    worker.getValue1().handle(event2);
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
