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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.javatuples.Pair;

public final class WorkersQueue implements Handler<Void> {

    public interface WorkersQueueListener {

        void poolEmpty(final WorkersQueue instance);

    };

    private final Queue<Pair<Consumer<Handler<AsyncResult<Void>>>, Handler<AsyncResult<Void>>>> workers = new LinkedList<>();
    private final Set<WorkersQueueListener> listeners = new HashSet();
    private final AtomicInteger concurrency = new AtomicInteger(0);
    private final AtomicInteger current = new AtomicInteger(0);

    WorkersQueue() {
        this.concurrency.set(5);
    }

    WorkersQueue(int concurrency) {
        this.concurrency.set(concurrency);
    }

    /**
     * @return The concurrency limit
     */
    public int getConcurrency() {
        return concurrency.get();
    }

    /**
     * @param concurrency  Define concurrencu limit for workers
     */
    public void setConcurrency(int concurrency) {
        this.concurrency.set(concurrency);
    }

    /**
     * @return Number of running workers
     */
    public int getRunning() {
        return current.get();
    }

    /**
     * Add a consumer in the pool
     * @param consumer The worker to run
     * @param handler Result handler associated with the declared consumer
     * @return 
     */
    public boolean add(final Consumer<Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler) {
        return workers.add(new Pair(consumer, handler));
    }

    /**
     * @param listener Listener to add
     * @return True if listener is added
     */
    public boolean add(final WorkersQueueListener listener) {
        return listeners.add(listener);
    }

    /**
     * @param listener Listener to remove
     * @return True if listener was removed
     */
    public boolean remove(final WorkersQueueListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void handle(Void event) {
        if (workers.isEmpty()) {
            fireEmptyPool();
        } else if (current.get() < concurrency.get()) {
            final Pair<Consumer<Handler<AsyncResult<Void>>>, Handler<AsyncResult<Void>>> worker = workers.poll();
            Vertx.currentContext().runOnContext(event1 -> {
                current.incrementAndGet();
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
            Vertx.currentContext().runOnContext(event -> {
                listener.poolEmpty(this);
            });
        });
    }
}
