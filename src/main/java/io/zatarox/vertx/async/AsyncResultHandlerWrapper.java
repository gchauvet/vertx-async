/*
 * Copyright 2004-2016 Guillaume Chauvet.
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

public final class AsyncResultHandlerWrapper<T, R> implements Handler<AsyncResult<R>> {

    private final Handler<AsyncResult<T>> handler;

    public AsyncResultHandlerWrapper(Handler<AsyncResult<T>> handler) {
        this.handler = handler;
    }

    @Override
    public void handle(AsyncResult<R> asyncResult) {
        if (asyncResult.failed()) {
            handler.handle(DefaultAsyncResult.fail(asyncResult.cause()));
        } else {
            handler.handle(DefaultAsyncResult.succeed((T) asyncResult.result()));
        }
    }
}
