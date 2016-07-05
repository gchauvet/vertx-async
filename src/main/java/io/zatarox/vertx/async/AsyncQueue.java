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

public interface AsyncQueue<T> {

    public interface AsyncQueueListener {

        void poolEmpty(final AsyncQueueImpl instance);

    };

    /**
     * Add a consumer in the pool
     *
     * @param task The task to run
     * @param handler Result handler associated with the declared consumer
     * @param top Add to the top of the queue ?
     * @return True is operation is successful
     */
    boolean add(final T task, final Handler<AsyncResult<Void>> handler, final boolean top);

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
    
    /**
     * @return Returning false if there are items waiting or being processed, or true if not.
     */
    boolean isIdle();
    
    /**
     * Removes the drain callback and empties remaining tasks from the queue forcing it to go idle.
     */
    void clear();
    
    /**
     * @return A boolean for determining whether the queue is in a paused state.
     */
    boolean isPaused();
    
    /**
     * @param paused  A function that pauses or not the processing of tasks.
     */
    void setPaused(final boolean paused);

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
