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
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(VertxUnitRunner.class)
public final class AsyncQueueImplTest {

    /**
     * Limits
     */
    private static final int TIMEOUT_LIMIT = 1000;
    private static final int REPEAT_LIMIT = 100;

    private BiConsumer<Integer, Handler<AsyncResult<Void>>> worker;
    private AsyncQueueImpl<Integer> queue;

    @Rule
    public RepeatRule repeater = new RepeatRule();
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp(final TestContext context) {
        worker = (t, u) -> {
            rule.vertx().setTimer(t, event -> {
                u.handle(DefaultAsyncResult.succeed());
            });
        };
        queue = new AsyncQueueImpl(worker);
        context.assertNotNull(queue);
        context.assertEquals(0, queue.getRunning());
        context.assertEquals(5, queue.getConcurrency());
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeEmptyQueue(final TestContext context) {
        context.assertTrue(queue.isIdle());
        rule.vertx().runOnContext(queue);
        context.assertEquals(0, queue.getRunning());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeConcurrency() {
        queue.setConcurrency(0);
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
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
        context.assertTrue(queue.add(listener));
        context.assertFalse(queue.add(listener));
        queue.add(100, event -> {
            context.assertTrue(event.succeeded());
            context.assertTrue(empty.get());
        }, false);
        context.assertFalse(queue.isIdle());
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeOneTaskSucceedInQueue(final TestContext context) {
        final Async async = context.async();
        context.assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
            async.complete();
        }, false));
        context.assertFalse(queue.isIdle());
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeTwoTaskSucceedWithOneWorker(final TestContext context) {
        final Async async = context.async();
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final AsyncQueueImpl<Integer> queue = new AsyncQueueImpl(worker, 1);
        context.assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
        }, false));
        context.assertTrue(queue.add(200, event -> {
            context.assertTrue(event.succeeded());
            async.complete();
        }, false));
        context.assertFalse(queue.isIdle());
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeTwoTaskSucceedWithDefaultNumberWorkers(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger();
        context.assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
            counter.incrementAndGet();
        }, false));
        context.assertTrue(queue.add(200, event -> {
            context.assertTrue(event.succeeded());
            async.complete();
        }, false));
        context.assertFalse(queue.isIdle());
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeOneTaskFailedInQueue(final TestContext context) {
        final Async async = context.async();
        queue = new AsyncQueueImpl<>((t, u) -> {
            rule.vertx().setTimer(t, event -> {
                u.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
            });
        });
        context.assertTrue(queue.add(100, event -> {
            context.assertFalse(event.succeeded());
            context.assertTrue(event.cause() instanceof IllegalArgumentException);
            async.complete();
        }, false));
        context.assertFalse(queue.isIdle());
    }
    
    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executePauseAndUnpause(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger();
        queue.setPaused(true);
        context.assertTrue(queue.isPaused());
        context.assertTrue(queue.isIdle());
        context.assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
            counter.incrementAndGet();
        }, false));
        context.assertTrue(queue.add(200, event -> {
            context.assertTrue(event.succeeded());
            context.assertFalse(queue.isPaused());
            async.complete();
        }, false));
        context.assertEquals(0, queue.getRunning());
        context.assertFalse(queue.isIdle());
        queue.setPaused(false);
        context.assertFalse(queue.isIdle());
    }
    
    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeAddToTop(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger();
        queue = new AsyncQueueImpl(worker, 1);
        queue.setPaused(true);
        context.assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
            context.assertEquals(2, counter.incrementAndGet());
            async.complete();
        }, false));
        context.assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
            context.assertFalse(queue.isPaused());
            context.assertEquals(1, counter.incrementAndGet());
        }, true));
        context.assertEquals(0, queue.getRunning());
        context.assertFalse(queue.isIdle());
        queue.setPaused(false);
        context.assertFalse(queue.isIdle());
    }
    
    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeClear(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger();
        queue.setPaused(true);
        context.assertTrue(queue.add(100, event -> {
            context.fail();
        }, false));
        context.assertEquals(0, queue.getRunning());
        context.assertFalse(queue.isIdle());
        queue.clear();
        queue.setPaused(false);
        context.assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
            context.assertFalse(queue.isPaused());
            context.assertEquals(1, counter.incrementAndGet());
            async.complete();
        }, false));
    }

}
