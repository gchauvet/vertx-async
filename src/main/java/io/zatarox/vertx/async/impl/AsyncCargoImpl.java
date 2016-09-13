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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AsyncCargoImpl<T> extends AbstractWorkerImpl<Collection<T>> {

    private final BiHandler<Collection<Pair<T, Handler<AsyncResult<Void>>>>, Handler<AsyncResult<Void>>> worker;
    private final Deque<Pair<T, Handler<AsyncResult<Void>>>> tasks = new ConcurrentLinkedDeque();

    public AsyncCargoImpl(final BiHandler<Collection<Pair<T, Handler<AsyncResult<Void>>>>, Handler<AsyncResult<Void>>> worker, final int payload) {
        super(payload);
        this.worker = worker;
    }

    public AsyncCargoImpl(final BiHandler<Collection<Pair<T, Handler<AsyncResult<Void>>>>, Handler<AsyncResult<Void>>> worker) {
        this(worker, Integer.MAX_VALUE);
    }

    @Override
    public boolean add(final Collection<T> tasks, final Handler<AsyncResult<Void>> handler, final boolean top) {
        try {
            final AtomicBoolean result = new AtomicBoolean(true);
            tasks.stream().forEach(t -> {
                result.set(result.get() && this.addInternal(t, handler, top));
            });
            return result.get();
        } finally {
            if (current.get() < 1 && !paused.get()) {
                Vertx.currentContext().runOnContext(this);
            }
        }
    }

    private boolean addInternal(final T task, final Handler<AsyncResult<Void>> handler, final boolean top) {
        final Pair<T, Handler<AsyncResult<Void>>> item = new PairImpl(task, handler);
        final boolean result;
        if (!top) {
            result = tasks.offer(item);
        } else {
            result = tasks.offerFirst(item);
        }
        return result;
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
        } else if (!paused.get()) {
            final int size = tasks.size() < concurrency.get() ? tasks.size() : concurrency.get();
            final Collection<Pair<T, Handler<AsyncResult<Void>>>> tasksToPass = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                tasksToPass.add(tasks.poll());
            }
            current.incrementAndGet();
            Vertx.currentContext().runOnContext(event1 -> {
                worker.handle(tasksToPass, event2 -> {
                    current.decrementAndGet();
                    this.handle(event1);
                });
            });
        }
    }

}
