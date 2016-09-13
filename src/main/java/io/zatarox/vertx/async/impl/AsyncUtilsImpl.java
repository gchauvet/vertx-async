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

import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import io.zatarox.vertx.async.api.AsyncMemoize;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.zatarox.vertx.async.api.AsyncUtils;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class AsyncUtilsImpl implements AsyncUtils {

    private final Context context;
    
    public AsyncUtilsImpl(final Context context)  {
        this.context = context;
    }

    @Override
    public <T> void timeout(final Handler<Handler<AsyncResult<T>>> function, final TimeUnit unit, final long delay, final Handler<AsyncResult<T>> handler) {
        context.executeBlocking(futur -> {
            final AtomicBoolean timeout = new AtomicBoolean(false);
            final long timerHandler = context.owner().setTimer(unit.toMillis(delay), id -> {
                timeout.set(true);
                futur.fail(new TimeoutException());
            });
            function.handle(event -> {
                context.owner().cancelTimer(timerHandler);
                if (!timeout.get()) {
                    handler.handle(event);
                }
            });
        }, handler);
    }

    @Override
    public <I, O> AsyncMemoize<I, O> memoize(final BiConsumer<I, Handler<AsyncResult<O>>> function) {
        return new AsyncMemoizeImpl(function);
    }

    @Override
    public <T> Handler<Handler<AsyncResult<T>>> constant(final T value) {
        return handler -> {
            handler.handle(DefaultAsyncResult.succeed(value));
        };
    }

    @Override
    public <I, O> BiConsumer<I, Handler<AsyncResult<O>>> asyncify(final Function<I, O> function) {
        return (item, handler) -> {
            try {
                handler.handle(DefaultAsyncResult.succeed(function.apply(item)));
            } catch (Throwable ex) {
                handler.handle(DefaultAsyncResult.fail(ex));
            }
        };
    }

}
