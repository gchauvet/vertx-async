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
import java.util.concurrent.atomic.AtomicLong;

public final class LoopRetryOptions<T> extends AbstractRetryOptions<T> {

    public LoopRetryOptions(long tries) {
        super(tries);
    }

    @Override
    public Handler<Void> build(final Handler<Handler<AsyncResult<T>>> task, final Handler<AsyncResult<T>> handler) {
        return new Handler<Void>() {
            final AtomicLong counter = new AtomicLong(tries);

            @Override
            public void handle(Void event) {
                task.handle(event1 -> { 
                    if (event1.failed()) {
                        if (counter.decrementAndGet() < 1) {
                            handler.handle(event1);
                        } else {
                            Vertx.currentContext().runOnContext(this);
                        }
                    } else {
                        handler.handle(event1);
                    }
                });
            }
        };
    }
}
