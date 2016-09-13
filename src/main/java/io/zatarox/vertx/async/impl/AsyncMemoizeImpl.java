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
import io.zatarox.vertx.async.api.AsyncMemoize;
import io.zatarox.vertx.async.api.BiHandler;
import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AsyncMemoizeImpl<I, O> implements AsyncMemoize<I, O> {

    private final Map<I, O> cache = new ConcurrentHashMap<>();
    private final BiHandler<I, Handler<AsyncResult<O>>> consumer;

    public AsyncMemoizeImpl(final BiHandler<I, Handler<AsyncResult<O>>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public O get(I argument) {
        return cache.get(argument);
    }

    @Override
    public boolean unset(I argument) {
        return cache.remove(argument) != null;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public void accept(I item, Handler<AsyncResult<O>> handler) {
        if (cache.containsKey(item)) {
            handler.handle(DefaultAsyncResult.succeed(cache.get(item)));
        } else {
            Vertx.currentContext().runOnContext(event -> {
                consumer.handle(item, event1 -> {
                    if (event1.succeeded()) {
                        cache.put(item, event1.result());
                    }
                    handler.handle(event1);
                });
            });
        }
    }
}
