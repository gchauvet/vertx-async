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
import io.zatarox.vertx.async.AsyncQueue.AsyncQueueListener;
import io.zatarox.vertx.async.DefaultAsyncResult;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    @Before
    public void setUp() {
        worker = (t, u) -> {
            rule.vertx().setPeriodic(t, event -> {
                u.handle(DefaultAsyncResult.succeed());
            });
        };
        queue = new AsyncQueueImpl(worker);
        assertNotNull(queue);
        assertEquals(0, queue.getRunning());
        assertEquals(5, queue.getConcurrency());
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeEmptyQueue() {
        rule.vertx().runOnContext(queue);
        assertEquals(0, queue.getRunning());
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
        final AsyncQueueListener listener = new AsyncQueueListener() {
            @Override
            public void poolEmpty(AsyncQueueImpl instance) {
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
        });
        rule.vertx().runOnContext(queue);
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeOneTaskSucceedInQueue(final TestContext context) {
        final Async async = context.async();
        assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
            async.complete();
        }));
        rule.vertx().runOnContext(queue);
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeTwoTaskSucceedWithOneWorker(final TestContext context) {
        final Async async = context.async();
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final AsyncQueueImpl<Integer> queue = new AsyncQueueImpl(worker, 1);
        assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
        }));
        assertTrue(queue.add(200, event -> {
            context.assertTrue(event.succeeded());
            async.complete();
        }));
        rule.vertx().runOnContext(queue);
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeTwoTaskSucceedWithDefaultNumberWorkers(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger();
        assertTrue(queue.add(100, event -> {
            context.assertTrue(event.succeeded());
            counter.incrementAndGet();
        }));
        assertTrue(queue.add(200, event -> {
            context.assertTrue(event.succeeded());
            async.complete();
        }));
        rule.vertx().runOnContext(queue);
    }

    @Test(timeout = AsyncQueueImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncQueueImplTest.REPEAT_LIMIT)
    public void executeOneTaskFailedInQueue(final TestContext context) {
        final Async async = context.async();
        queue = new AsyncQueueImpl<>((t, u) -> {
            rule.vertx().setPeriodic(t, event -> {
                u.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
            });
        });
        assertTrue(queue.add(100, event -> {
            context.assertFalse(event.succeeded());
            context.assertTrue(event.cause() instanceof IllegalArgumentException);
            async.complete();
        }));
        rule.vertx().runOnContext(queue);
    }

}
