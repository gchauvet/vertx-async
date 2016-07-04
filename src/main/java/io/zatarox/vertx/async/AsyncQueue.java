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
package io.zatarox.vertx.async;

import io.zatarox.vertx.async.impl.AsyncQueueImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.util.function.Consumer;

public interface AsyncQueue {

    public interface AsyncQueueListener {

        void poolEmpty(final AsyncQueueImpl instance);

    };

    /**
     * Add a consumer in the pool
     *
     * @param consumer The worker to run
     * @param handler Result handler associated with the declared consumer
     * @return
     */
    boolean add(final Consumer<Handler<AsyncResult<Void>>> consumer, final Handler<AsyncResult<Void>> handler);

    /**
     * @param listener Listener to add
     * @return True if listener is added
     */
    boolean add(final AsyncQueueListener listener);

    /**
     * @return The concurrency limit
     */
    int getConcurrency();

    /**
     * @return Number of running workers
     */
    int getRunning();

    void handle(Void event);

    /**
     * @param listener Listener to remove
     * @return True if listener was removed
     */
    boolean remove(final AsyncQueueListener listener);

    /**
     * @param concurrency Define concurrencu limit for workers
     */
    void setConcurrency(int concurrency);

}
