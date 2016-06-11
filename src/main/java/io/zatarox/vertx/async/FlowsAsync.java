/*
 * The MIT License
 *
 * Copyright 2016 Guillaume Chauvet.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.zatarox.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.function.Consumer;

public class FlowsAsync {

    private FlowsAsync() {
    }

    public static <T> void retry(final Vertx instance, Consumer<Handler<AsyncResult<T>>> task, final long times, final Handler<AsyncResult<T>> handler) {
        instance.runOnContext((Void event) -> {
            task.accept((Handler<AsyncResult<T>>) new Handler<AsyncResult<T>>() {
                final ObjectWrapper<Integer> count = new ObjectWrapper<>(0);

                @Override
                public void handle(AsyncResult<T> result) {
                    if (result.failed()) {
                        count.setObject(count.getObject() + 1);
                        if (count.getObject() > times) {
                            handler.handle(DefaultAsyncResult.fail(result));
                        } else {
                            instance.runOnContext((Void event) -> {
                                task.accept(this);
                            });
                        }
                    } else {
                        handler.handle(DefaultAsyncResult.succeed(result.result()));
                    }
                }
            });
        });
    }

    public static <T> void forever(final Vertx instance, Consumer<Handler<AsyncResult<T>>> task, final Handler<AsyncResult<T>> handler) {
        instance.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                task.accept((result) -> {
                    if (result.failed()) {
                        handler.handle(DefaultAsyncResult.fail(result));
                    } else {
                        instance.runOnContext(this);
                    }
                });
            }
        });
    }

}
