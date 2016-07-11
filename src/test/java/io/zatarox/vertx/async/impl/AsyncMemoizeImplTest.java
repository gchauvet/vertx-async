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

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zatarox.vertx.async.AsyncFlows;
import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(VertxUnitRunner.class)
public final class AsyncMemoizeImplTest {

    /**
     * Limits
     */
    private static final int TIMEOUT_LIMIT = 1000;
    private static final int REPEAT_LIMIT = 100;

    @Rule
    public RepeatRule repeater = new RepeatRule();
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Test(timeout = AsyncMemoizeImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncMemoizeImplTest.REPEAT_LIMIT)
    public void executeSucceedAndClear(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger(0);
        final AsyncMemoizeImpl<Integer, Integer> memoize = new AsyncMemoizeImpl<>((item, handler) -> {
            counter.incrementAndGet();
            handler.handle(DefaultAsyncResult.succeed(item + 1));
        });

        context.assertTrue(memoize.isEmpty());
        memoize.accept(1, event -> {
            context.assertTrue(event.succeeded());
            context.assertFalse(memoize.isEmpty());
            context.assertEquals(2, memoize.get(1));
            context.assertEquals(1, counter.get());
            memoize.clear();
            context.assertTrue(memoize.isEmpty());
            async.complete();
        });
    }

    @Test(timeout = AsyncMemoizeImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncMemoizeImplTest.REPEAT_LIMIT)
    public void executeSucceedAndUnset(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger(0);
        final AsyncMemoizeImpl<Integer, Integer> memoize = new AsyncMemoizeImpl<>((item, handler) -> {
            counter.incrementAndGet();
            handler.handle(DefaultAsyncResult.succeed(item + 1));
        });

        context.assertTrue(memoize.isEmpty());
        memoize.accept(1, event -> {
            context.assertTrue(event.succeeded());
            context.assertFalse(memoize.isEmpty());
            context.assertEquals(2, memoize.get(1));
            context.assertEquals(1, counter.get());
            context.assertTrue(memoize.unset(1));
            context.assertFalse(memoize.unset(1));
            context.assertTrue(memoize.isEmpty());
            async.complete();
        });
    }

    @Test(timeout = AsyncMemoizeImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncMemoizeImplTest.REPEAT_LIMIT)
    public void executeSucceedAndTestCache(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger(0);
        final AsyncMemoizeImpl<Integer, Integer> memoize = new AsyncMemoizeImpl<>((item, handler) -> {
            counter.incrementAndGet();
            handler.handle(DefaultAsyncResult.succeed(item + 1));
        });

        context.assertTrue(memoize.isEmpty());
        AsyncFlows.<Void, Void>seq((t, u) -> {
            memoize.accept(1, event -> {
                context.assertTrue(event.succeeded());
                context.assertFalse(memoize.isEmpty());
                context.assertEquals(2, memoize.get(1));
                context.assertEquals(1, counter.get());
                u.handle(DefaultAsyncResult.succeed());
            });
        }, (t, u) -> {
            memoize.accept(1, event -> {
                context.assertTrue(event.succeeded());
                context.assertFalse(memoize.isEmpty());
                context.assertEquals(2, memoize.get(1));
                context.assertEquals(1, counter.get());
                context.assertTrue(memoize.unset(1));
                context.assertFalse(memoize.unset(1));
                context.assertTrue(memoize.isEmpty());
                u.handle(DefaultAsyncResult.succeed());
            });
        }).accept(null, event -> {
            async.complete();
        });
    }

    @Test(timeout = AsyncMemoizeImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncMemoizeImplTest.REPEAT_LIMIT)
    public void executeFailedAndClear(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger(0);
        final AsyncMemoizeImpl<Integer, Integer> memoize = new AsyncMemoizeImpl<>((item, handler) -> {
            counter.incrementAndGet();
            handler.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
        });
        memoize.accept(1, event -> {
            context.assertFalse(event.succeeded());
            context.assertTrue(memoize.isEmpty());
            context.assertNull(memoize.get(1));
            context.assertEquals(1, counter.get());
            memoize.clear();
            context.assertTrue(memoize.isEmpty());
            async.complete();
        });
    }

    @Test(timeout = AsyncMemoizeImplTest.TIMEOUT_LIMIT)
    @Repeat(AsyncMemoizeImplTest.REPEAT_LIMIT)
    public void executeFailedAndUnset(final TestContext context) {
        final Async async = context.async();
        final AtomicInteger counter = new AtomicInteger(0);
        final AsyncMemoizeImpl<Integer, Integer> memoize = new AsyncMemoizeImpl<>((item, handler) -> {
            counter.incrementAndGet();
            handler.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
        });

        context.assertTrue(memoize.isEmpty());
        memoize.accept(1, event -> {
            context.assertFalse(event.succeeded());
            context.assertTrue(memoize.isEmpty());
            context.assertNull(memoize.get(1));
            context.assertEquals(1, counter.get());
            context.assertTrue(memoize.isEmpty());
            context.assertFalse(memoize.unset(1));
            context.assertTrue(memoize.isEmpty());
            async.complete();
        });
    }

}
