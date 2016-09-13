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
import io.zatarox.vertx.async.api.BiHandler;
import io.zatarox.vertx.async.api.Pair;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class AsyncQueueImpl<T> extends AbstractWorkerImpl<T> {

    private final BiHandler<T, Handler<AsyncResult<Void>>> worker;
    private final Deque<Pair<T, Handler<AsyncResult<Void>>>> tasks = new ConcurrentLinkedDeque();

    public AsyncQueueImpl(final BiHandler<T, Handler<AsyncResult<Void>>> worker) {
        this(worker, 5);
    }

    public AsyncQueueImpl(final BiHandler<T, Handler<AsyncResult<Void>>> worker, final int concurrency) {
        super(concurrency);
        this.worker = worker;
    }

    public boolean add(final T task, final Handler<AsyncResult<Void>> handler, final boolean top) {
        try {
            final Pair<T, Handler<AsyncResult<Void>>> item = new PairImpl(task, handler);
            final boolean result;
            if (!top) {
                result = tasks.offer(item);
            } else {
                result = tasks.offerFirst(item);
            }
            return result;
        } finally {
            if (current.get() < 1 && !paused.get()) {
                Vertx.currentContext().runOnContext(this);
            }
        }
    }

    @Override
    public boolean isIdle() {
        return current.get() == 0 && tasks.isEmpty();
    }

    @Override
    public void clear() {
        tasks.clear();
    }

    public void handle(Void event) {
        if (tasks.isEmpty()) {
            fireEmptyPool();
        } else if (current.get() < concurrency.get() && !paused.get()) {
            final Pair<T, Handler<AsyncResult<Void>>> task = tasks.poll();
            current.incrementAndGet();
            Vertx.currentContext().runOnContext(event1 -> {
                worker.handle(task.getKey(), event2 -> {
                    task.getValue().handle(event2);
                    current.decrementAndGet();
                    this.handle(event);
                });
            });
            this.handle(event);
        }
    }

}
