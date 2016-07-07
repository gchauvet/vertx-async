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
import java.util.function.BiConsumer;
import org.javatuples.Pair;

public final class AsyncQueueImpl<T> extends AbstractWorkerImpl<T> {

    public AsyncQueueImpl(final BiConsumer<T, Handler<AsyncResult<Void>>> worker) {
        super(worker, 5);
    }

    public AsyncQueueImpl(final BiConsumer<T, Handler<AsyncResult<Void>>> worker, final int concurrency) {
        super(worker, concurrency);
    }

    public void handle(Void event) {
        if (tasks.isEmpty()) {
            fireEmptyPool();
        } else if (current.get() < concurrency.get() && !paused.get()) {
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

}
