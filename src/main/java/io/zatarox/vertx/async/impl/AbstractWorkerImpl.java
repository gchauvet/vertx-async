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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import io.zatarox.vertx.async.api.AsyncWorker;

public abstract class AbstractWorkerImpl<T> implements AsyncWorker<T>, Handler<Void> {

    protected final Set<AsyncWorkerListener> listeners = new ConcurrentHashSet();
    protected final AtomicInteger concurrency = new AtomicInteger(0);
    protected final AtomicBoolean paused = new AtomicBoolean(false);
    protected final AtomicInteger current = new AtomicInteger(0);

    protected AbstractWorkerImpl(final int concurrency) {
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

    public int getRunning() {
        return current.get();
    }

    public boolean add(final AsyncWorkerListener listener) {
        return listeners.add(listener);
    }

    public boolean remove(final AsyncWorkerListener listener) {
        return listeners.remove(listener);
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
        listeners.stream().forEach(listener -> {
            listener.poolEmpty(this);
        });
    }

}
