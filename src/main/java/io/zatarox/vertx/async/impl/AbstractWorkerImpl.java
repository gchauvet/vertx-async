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
import io.zatarox.vertx.async.Workers;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.javatuples.Pair;

public abstract class AbstractWorkerImpl<T> implements Workers<T>, Handler<Void> {

    protected final BiConsumer<T, Handler<AsyncResult<Void>>> worker;
    protected final Deque<Pair<T, Handler<AsyncResult<Void>>>> tasks = new ConcurrentLinkedDeque();
    protected final Set<AsyncWorkerListener> listeners = new ConcurrentHashSet();
    protected final AtomicInteger concurrency = new AtomicInteger(0);
    protected final AtomicBoolean paused = new AtomicBoolean(false);
    protected final AtomicInteger current = new AtomicInteger(0);

    public AbstractWorkerImpl(final BiConsumer<T, Handler<AsyncResult<Void>>> worker, final int concurrency) {
        this.worker = worker;
        this.concurrency.set(concurrency);
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

    public int getRunning() {
        return current.get();
    }

    public boolean add(final T task, final Handler<AsyncResult<Void>> handler, final boolean top) {
        try {
            final Pair<T, Handler<AsyncResult<Void>>> item = new Pair(task, handler);
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

    public boolean add(final AsyncWorkerListener listener) {
        return listeners.add(listener);
    }

    public boolean remove(final AsyncWorkerListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public boolean isIdle() {
        return current.get() == 0 && tasks.isEmpty();
    }

    @Override
    public void clear() {
        tasks.clear();
    }

    @Override
    public boolean isPaused() {
        return paused.get();
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused.set(paused);
        if (!paused && current.get() < 1) {
            Vertx.currentContext().runOnContext(this);
        }
    }

    protected void fireEmptyPool() {
        listeners.stream().forEach((AsyncWorkerListener listener) -> {
            listener.poolEmpty(this);
        });
    }

}
