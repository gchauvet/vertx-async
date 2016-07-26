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
package io.zatarox.vertx.async.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zatarox.vertx.async.AsyncFactorySingleton;
import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.zatarox.vertx.async.api.AsyncWorker.AsyncWorkerListener;
import io.zatarox.vertx.async.api.AsyncWorker;
import java.util.Arrays;
import java.util.Collection;
import org.javatuples.Pair;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(VertxUnitRunner.class)
public final class AsyncCargoImplTest {

    /**
     * Limits
     */
    private static final int TIMEOUT_LIMIT = 1000;
    private static final int REPEAT_LIMIT = 100;

    private BiConsumer<Collection<Pair<Integer, Handler<AsyncResult<Void>>>>, Handler<AsyncResult<Void>>> worker;
    private AsyncCargoImpl<Integer> cargo;

    @Rule
    public RepeatRule repeater = new RepeatRule();
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Before
    public void setUp(final TestContext context) {
        worker = (Collection<Pair<Integer, Handler<AsyncResult<Void>>>> items, Handler<AsyncResult<Void>> handler) -> {
            AsyncFactorySingleton.getInstance().createCollections(rule.vertx().getOrCreateContext()).each(items, (item, callback) -> {
                rule.vertx().setTimer(item.getValue0(), event -> {
                    item.getValue1().handle(DefaultAsyncResult.succeed());
                });
                callback.handle(DefaultAsyncResult.succeed());
            }, handler);
        };
        cargo = new AsyncCargoImpl(worker, 5);
        context.assertNotNull(cargo);
        context.assertEquals(0, cargo.getRunning());
        context.assertEquals(5, cargo.getConcurrency());
    }

    @Test(timeout = AsyncCargoImplTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncCargoImplTest.REPEAT_LIMIT, silent = true)
    public void executeEmptyCargo(final TestContext context) {
        context.assertTrue(cargo.isIdle());
        rule.vertx().runOnContext(cargo);
        context.assertEquals(0, cargo.getRunning());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeConcurrency() {
        cargo.setConcurrency(0);
    }

    @Test(timeout = AsyncCargoImplTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncCargoImplTest.REPEAT_LIMIT, silent = true)
    public void testListeners(final TestContext context) {
        final Async async = context.async();
        final AtomicBoolean empty = new AtomicBoolean(false);
        final AsyncWorkerListener listener = new AsyncWorkerListener() {
            @Override
            public void poolEmpty(AsyncWorker instance) {
                context.assertNotNull(instance);
                empty.set(true);
                context.assertTrue(instance.remove(this));
                context.assertFalse(instance.remove(this));
                async.complete();
            }
        };
        context.assertTrue(cargo.add(listener));
        context.assertFalse(cargo.add(listener));
        cargo.add(Arrays.asList(100), event -> {
            context.assertTrue(event.succeeded());
            context.assertTrue(empty.get());
        }, false);
        context.assertFalse(cargo.isIdle());
    }

    @Test(timeout = AsyncCargoImplTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncCargoImplTest.REPEAT_LIMIT, silent = true)
    public void executeOneTaskSucceedInCargo(final TestContext context) {
        final Async async = context.async();
        context.assertTrue(cargo.add(Arrays.asList(100), event -> {
            context.assertTrue(event.succeeded());
            async.complete();
        }, false));
        context.assertFalse(cargo.isIdle());
    }

    @Test(timeout = AsyncCargoImplTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncCargoImplTest.REPEAT_LIMIT, silent = true)
    public void executeTwoTaskSucceedWithOneWorker(final TestContext context) {
        final Async async = context.async();
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final AtomicInteger counter = new AtomicInteger();
        final AsyncCargoImpl<Integer> queue = new AsyncCargoImpl(worker, 1);
        context.assertTrue(queue.add(Arrays.asList(100, 200), event -> {
            context.assertTrue(event.succeeded());
            context.assertTrue(counter.incrementAndGet() <= 2);
            if (counter.get() == 2) {
                async.complete();
            }
        }, false));
        context.assertFalse(queue.isIdle());
    }

    @Test(timeout = AsyncCargoImplTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncCargoImplTest.REPEAT_LIMIT, silent = true)
    public void executeTwoTaskSucceedWithDefaultNumberWorkers(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger();
        context.assertTrue(cargo.add(Arrays.asList(100, 200), event -> {
            context.assertTrue(event.succeeded());
            context.assertTrue(counter.incrementAndGet() <= 2);
            if (counter.get() == 2) {
                async.complete();
            }
        }, false));
        context.assertFalse(cargo.isIdle());
    }

    @Test(timeout = AsyncCargoImplTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncCargoImplTest.REPEAT_LIMIT, silent = true)
    public void executeOneTaskFailedInCargo(final TestContext context) {
        final Async async = context.async();
        cargo = new AsyncCargoImpl<>((tasks, handler) -> {
            AsyncFactorySingleton.getInstance().createCollections(rule.vertx().getOrCreateContext()).each(tasks, (task, callback) -> {
                task.getValue1().handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
                callback.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
            }, handler);
        });
        context.assertTrue(cargo.add(Arrays.asList(100), event -> {
            context.assertFalse(event.succeeded());
            context.assertTrue(event.cause() instanceof IllegalArgumentException);
            async.complete();
        }, false));
        context.assertFalse(cargo.isIdle());
    }

    @Test(timeout = AsyncCargoImplTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncCargoImplTest.REPEAT_LIMIT, silent = true)
    public void executePauseAndUnpause(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger();
        cargo.setPaused(true);
        context.assertTrue(cargo.isPaused());
        context.assertTrue(cargo.isIdle());
        context.assertTrue(cargo.add(Arrays.asList(100, 200), event -> {
            context.assertTrue(event.succeeded());
            context.assertFalse(cargo.isPaused());
            context.assertTrue(counter.incrementAndGet() <= 2);
            if (counter.get() == 2) {
                async.complete();
            }
        }, false));
        context.assertEquals(0, cargo.getRunning());
        context.assertFalse(cargo.isIdle());
        cargo.setPaused(false);
        context.assertFalse(cargo.isIdle());
    }

    @Test(timeout = AsyncCargoImplTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncCargoImplTest.REPEAT_LIMIT, silent = true)
    public void executeAddToTop(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger();
        cargo = new AsyncCargoImpl(worker, 1);
        cargo.setPaused(true);
        context.assertTrue(cargo.add(Arrays.asList(200, 100), event -> {
            context.assertTrue(event.succeeded());
            context.assertFalse(cargo.isPaused());
            context.assertTrue(counter.incrementAndGet() <= 2);
            if (counter.get() == 2) {
                async.complete();
            }
        }, true));
        context.assertEquals(0, cargo.getRunning());
        context.assertFalse(cargo.isIdle());
        cargo.setPaused(false);
        context.assertFalse(cargo.isIdle());
    }

    @Test(timeout = AsyncCargoImplTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncCargoImplTest.REPEAT_LIMIT, silent = true)
    public void executeClear(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger();
        cargo.setPaused(true);
        context.assertTrue(cargo.add(Arrays.asList(200), event -> {
            context.fail();
        }, false));
        context.assertEquals(0, cargo.getRunning());
        context.assertFalse(cargo.isIdle());
        cargo.clear();
        cargo.setPaused(false);
        context.assertTrue(cargo.add(Arrays.asList(100), event -> {
            context.assertTrue(event.succeeded());
            context.assertFalse(cargo.isPaused());
            context.assertEquals(1, counter.incrementAndGet());
            async.complete();
        }, false));
    }

}
