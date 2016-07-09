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

public class FakeFailingAsyncSupplier<T> extends FakeAsyncSupplier<T> {

    private final int successCount;
    private final T result;
    private final Throwable cause;

    public FakeFailingAsyncSupplier(Throwable cause) {
        this(0, null, cause);
    }

    public FakeFailingAsyncSupplier(int successCount, T result, Throwable cause) {
        this.successCount = successCount;
        this.result = result;
        this.cause = cause;
    }

    @Override
    public void accept(Handler<AsyncResult<T>> handler) {
        incrementRunCount();

        if (runCount() > successCount) {
            handler.handle(DefaultAsyncResult.fail(cause));
        } else {
            handler.handle(DefaultAsyncResult.succeed(result));
        }
    }

    public Throwable cause() {
        return cause;
    }
}
