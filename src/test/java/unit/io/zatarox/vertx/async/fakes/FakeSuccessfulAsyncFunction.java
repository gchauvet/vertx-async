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
package io.zatarox.vertx.async.fakes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.zatarox.vertx.async.utils.DefaultAsyncResult;

public class FakeSuccessfulAsyncFunction<T, R> extends FakeAsyncFunction<T, R> {

    private final int failureCount;
    private final R result;
    private final Throwable cause;

    public FakeSuccessfulAsyncFunction(R result) {
        this(0, null, result);
    }

    public FakeSuccessfulAsyncFunction(int failureCount, Throwable cause, R result) {
        this.failureCount = failureCount;
        this.result = result;
        this.cause = cause;
    }

    @Override
    public void accept(T value, Handler<AsyncResult<R>> handler) {
        addConsumedValue(value);
        incrementRunCount();

        if (runCount() > failureCount) {
            handler.handle(DefaultAsyncResult.succeed(result));
        } else {
            handler.handle(DefaultAsyncResult.fail(cause));
        }
    }

    public R result() {
        return result;
    }
}
