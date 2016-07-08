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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class AsyncUtils {

    private AsyncUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    /**
     * Emulate a time limit on an asynchronous function. If the function does not
     * call its callback within the specified miliseconds, it will be called
     * with a timeout error.
     *
     * @param <T> Handled generic type
     * @param function A function which will be runned.
     * @param unit Time unit used for the delay time.
     * @param delay A time delay in the specified time unit.
     * @param handler An handler called when function finished or timeout is reached
     */
    public static <T> void timeout(final Consumer<Handler<AsyncResult<T>>> function, final TimeUnit unit, final long delay, final Handler<AsyncResult<T>> handler) {
        Vertx.currentContext().executeBlocking(futur -> {
            final AtomicBoolean timeout = new AtomicBoolean(false);
            final long timerHandler = Vertx.currentContext().owner().setTimer(unit.toMillis(delay), id -> {
                timeout.set(true);
                futur.fail(new TimeoutException());
            });
            function.accept(event -> {
                Vertx.currentContext().owner().cancelTimer(timerHandler);
                if (!timeout.get()) {
                    handler.handle(event);
                }
            });
        }, handler);
    }

}
