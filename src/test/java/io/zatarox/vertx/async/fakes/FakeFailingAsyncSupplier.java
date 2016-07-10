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
    private final RuntimeException cause;
    private final boolean catched;

    public FakeFailingAsyncSupplier(RuntimeException cause) {
        this(0, null, cause);
    }
    
    public FakeFailingAsyncSupplier(RuntimeException cause, boolean catched) {
        this(0, null, cause, catched);
    }

    public FakeFailingAsyncSupplier(int successCount, T result, RuntimeException cause) {
        this(successCount, result, cause, true);
    }
    
    public FakeFailingAsyncSupplier(int successCount, T result, RuntimeException cause, boolean catched) {
        this.successCount = successCount;
        this.result = result;
        this.cause = cause;
        this.catched = catched;
    }    

    @Override
    public void accept(Handler<AsyncResult<T>> handler) {
        incrementRunCount();

        if (runCount() > successCount) {
            if(catched) {
            handler.handle(DefaultAsyncResult.fail(cause));
            } else {
                throw cause;
            }
        } else {
            handler.handle(DefaultAsyncResult.succeed(result));
        }
    }

    public Throwable cause() {
        return cause;
    }
}
