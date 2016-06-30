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

import java.util.concurrent.TimeUnit;

/**
 * Thos class define parameters for a retry method call.
 * 
 */
public final class RetryOptions {

    private final TimeUnit unit;
    private final long delay;
    private final long tries;

    public RetryOptions(long tries) {
        this(tries, null, 0);
    }

    public RetryOptions(long tries, TimeUnit unit, long delay) {
        if (tries < 1) {
            throw new IllegalArgumentException("Tries must be positive");
        } else if (unit != null && delay < 1) {
            throw new IllegalArgumentException("Timeout delay must be positive");
        }
        this.tries = tries;
        this.unit = unit;
        this.delay = delay;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public long getDelay() {
        return delay;
    }

    public long getTries() {
        return tries;
    }

}
