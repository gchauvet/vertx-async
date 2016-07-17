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

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zatarox.vertx.async.api.*;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public final class AsyncFactorySingletonTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void onlyOneFactoryInstance(final TestContext context) {
        assertSame(AsyncFactorySingleton.getInstance(), AsyncFactorySingleton.getInstance());
    }

    @Test
    public void differentAsyncFlowsInstance(final TestContext context) {
        final AsyncFlows instance1 = AsyncFactorySingleton.getInstance().createFlows(rule.vertx().getOrCreateContext());
        final AsyncFlows instance2 = AsyncFactorySingleton.getInstance().createFlows(rule.vertx().getOrCreateContext());
        assertNotSame(instance1, instance2);
    }
    
    @Test
    public void differentAsyncCollectionsInstance(final TestContext context) {
        final AsyncCollections instance1 = AsyncFactorySingleton.getInstance().createCollections(rule.vertx().getOrCreateContext());
        final AsyncCollections instance2 = AsyncFactorySingleton.getInstance().createCollections(rule.vertx().getOrCreateContext());
        assertNotSame(instance1, instance2);
    }
    
    @Test
    public void differentAsyncUtilsInstance(final TestContext context) {
        final AsyncUtils instance1 = AsyncFactorySingleton.getInstance().createUtils(rule.vertx().getOrCreateContext());
        final AsyncUtils instance2 = AsyncFactorySingleton.getInstance().createUtils(rule.vertx().getOrCreateContext());
        assertNotSame(instance1, instance2);
    }

}
