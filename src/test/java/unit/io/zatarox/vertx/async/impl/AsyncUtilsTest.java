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
package io.zatarox.vertx.async.impl;

import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zatarox.vertx.async.api.AsyncUtils;
import io.zatarox.vertx.async.api.BiHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(VertxUnitRunner.class)
public final class AsyncUtilsTest {

    /**
     * Limits
     */
    private static final int TIMEOUT_LIMIT = 1500;
    private static final int REPEAT_LIMIT = 100;

    @Rule
    public RepeatRule repeater = new RepeatRule();
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();
    private AsyncUtils instance;

    @Before
    public void setUp(final TestContext context) {
        instance = new AsyncUtilsImpl(rule.vertx().getOrCreateContext());
    }

    @Test(timeout = AsyncUtilsTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncUtilsTest.REPEAT_LIMIT, silent = true)
    public void timeoutNotRaised(final TestContext context) {
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        instance.<Void>timeout(handler -> {
            handler.handle(DefaultAsyncResult.succeed());
        }, TimeUnit.MILLISECONDS, 100L, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncUtilsTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncUtilsTest.REPEAT_LIMIT, silent = true)
    public void timeoutNotRaisedWithError(final TestContext context) {
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        instance.<Void>timeout(handler -> {
            handler.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
        }, TimeUnit.MILLISECONDS, 100L, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertTrue(result.cause() instanceof IllegalArgumentException);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncUtilsTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncUtilsTest.REPEAT_LIMIT, silent = true)
    public void timeoutRaised(final TestContext context) {
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        instance.<Void>timeout(handler -> {
            rule.vertx().setTimer(1000, id -> {
                handler.handle(DefaultAsyncResult.succeed());
            });
        }, TimeUnit.MILLISECONDS, 100L, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertTrue(result.cause() instanceof TimeoutException);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncUtilsTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncUtilsTest.REPEAT_LIMIT, silent = true)
    public void timeoutRaisedWithError(final TestContext context) {
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        instance.<Void>timeout(handler -> {
            rule.vertx().setTimer(1000, id -> {
                handler.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
            });
        }, TimeUnit.MILLISECONDS, 100L, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertTrue(result.cause() instanceof TimeoutException);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncUtilsTest.TIMEOUT_LIMIT)
    public void createMemoize(final TestContext context) {
        instance.<Void, Void>memoize((item, handler) -> {
            handler.handle(DefaultAsyncResult.succeed(item));
        });
    }

    @Test(timeout = AsyncUtilsTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncUtilsTest.REPEAT_LIMIT, silent = true)
    public void constantWithNull(final TestContext context) {
        final Long value = (long) 73;
        final Handler<Handler<AsyncResult<Long>>> function = instance.constant(value);
        final Async async = context.async();
        context.assertNotNull(function);
        rule.vertx().runOnContext(event -> {
            function.handle(event1 -> {
                context.assertTrue(event1.succeeded());
                context.assertEquals(value, event1.result());
                async.complete();
            });
        });
    }

    @Test(timeout = AsyncUtilsTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncUtilsTest.REPEAT_LIMIT, silent = true)
    public void asyncifyAFunction(final TestContext context) {
        final Async async = context.async();
        final BiHandler<Integer, Handler<AsyncResult<Integer>>> function = instance.asyncify(t -> {
            return t + 1;
        });
        context.assertNotNull(function);
        rule.vertx().runOnContext(handler -> {
            function.handle(72, result -> {
                context.assertTrue(result.succeeded());
                context.assertEquals(73, result.result());
                async.complete();
            });
        });
    }

    @Test(timeout = AsyncUtilsTest.TIMEOUT_LIMIT)
    @Repeat(value = AsyncUtilsTest.REPEAT_LIMIT, silent = true)
    public void asyncifyAFunctionUnhandledException(final TestContext context) {
        final Async async = context.async();
        final BiHandler<Integer, Handler<AsyncResult<Integer>>> function = instance.asyncify(t -> {
            throw new RuntimeException();
        });
        context.assertNotNull(function);
        rule.vertx().runOnContext(handler -> {
            function.handle(72, result -> {
                context.assertFalse(result.succeeded());
                context.assertTrue(result.cause() instanceof RuntimeException);
                async.complete();
            });
        });
    }
}
