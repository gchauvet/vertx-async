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

import io.zatarox.vertx.async.api.AsyncFactory;
import io.zatarox.vertx.async.api.AsyncUtils;
import io.vertx.core.Context;
import io.zatarox.vertx.async.api.AsyncCollections;
import io.zatarox.vertx.async.api.AsyncFlows;
import io.zatarox.vertx.async.impl.AsyncCollectionsImpl;
import io.zatarox.vertx.async.impl.AsyncFlowsImpl;
import io.zatarox.vertx.async.impl.AsyncUtilsImpl;

public final class AsyncFactorySingleton implements AsyncFactory {

    private static AsyncFactorySingleton instance = null;

    private AsyncFactorySingleton() {
    }

    @Override
    public AsyncUtils createUtils(final Context context) {
        return new AsyncUtilsImpl(context);
    }

    @Override
    public AsyncCollections createCollections(final Context context) {
        return new AsyncCollectionsImpl(context);
    }

    @Override
    public AsyncFlows createFlows(final Context context) {
        return new AsyncFlowsImpl(context);
    }

    public static AsyncFactorySingleton getInstance() {
        if (instance == null) {
            instance = new AsyncFactorySingleton();
        }
        return instance;
    }

}
