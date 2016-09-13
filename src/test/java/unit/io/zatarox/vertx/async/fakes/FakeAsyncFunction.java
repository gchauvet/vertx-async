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
import io.zatarox.vertx.async.api.BiHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class FakeAsyncFunction<T, R> implements BiHandler<T, Handler<AsyncResult<R>>> {

    private final ArrayList<T> consumedValues = new ArrayList<>();
    private int runCount = 0;

    protected void incrementRunCount() {
        runCount++;
    }

    protected void addConsumedValue(T consumedValue) {
        this.consumedValues.add(consumedValue);
    }

    public int runCount() {
        return runCount;
    }

    public T consumedValue() {
        return consumedValues.get(consumedValues.size() - 1);
    }

    public List<T> consumedValues() {
        return consumedValues;
    }
}
