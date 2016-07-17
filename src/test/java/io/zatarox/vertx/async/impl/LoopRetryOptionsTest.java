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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(VertxUnitRunner.class)
public final class LoopRetryOptionsTest {

    /**
     * Limits
     */
    private static final int TIMEOUT_LIMIT = 1000;
    private static final int REPEAT_LIMIT = 100;

    private LoopRetryOptions options;

    @Rule
    public RepeatRule repeater = new RepeatRule();
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Before
    public void setUp() {
        options = new LoopRetryOptions(10);
        assertEquals(10, options.getTries());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeTry() {
        assertNull(new LoopRetryOptions(-1));
    }

    @Test(timeout = LoopRetryOptionsTest.TIMEOUT_LIMIT)
    @Repeat(value = LoopRetryOptionsTest.REPEAT_LIMIT, silent = true)
    public void retryExecutesTheTaskWithoutError(final TestContext context) {
        final Consumer<Handler<AsyncResult<String>>> task = mock(Consumer.class);
        final Async async = context.async();

        doAnswer(invocation -> {
            final Handler<AsyncResult<String>> handler = invocation.getArgumentAt(0, Handler.class);
            handler.handle(DefaultAsyncResult.succeed("TEST"));
            return null;
        }).when(task).accept(any(Handler.class));

        AsyncFactorySingleton.getInstance().createFlows(rule.vertx().getOrCreateContext()).retry(options, task, result -> {
            verify(task, times(1)).accept(any(Handler.class));
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals("TEST", result.result());
            async.complete();
        });
    }

    @Test(timeout = LoopRetryOptionsTest.TIMEOUT_LIMIT)
    @Repeat(value = LoopRetryOptionsTest.REPEAT_LIMIT, silent = true)
    public void retryExecutesAndFaildOnAllIteratee(final TestContext context) {
        final Consumer<Handler<AsyncResult<String>>> task = mock(Consumer.class);
        final Async async = context.async();

        doAnswer(invocation -> {
            final Handler<AsyncResult<String>> handler = invocation.getArgumentAt(0, Handler.class);
            handler.handle(DefaultAsyncResult.fail(new RuntimeException("Failed")));
            return null;
        }).when(task).accept(any(Handler.class));

        AsyncFactorySingleton.getInstance().createFlows(rule.vertx().getOrCreateContext()).retry(options, task, result -> {
            verify(task, times(10)).accept(any(Handler.class));
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            async.complete();
        });
    }

    @Test(timeout = LoopRetryOptionsTest.TIMEOUT_LIMIT)
    @Repeat(value = LoopRetryOptionsTest.REPEAT_LIMIT, silent = true)
    public void retryExecutesSuccessBeforeLastFailure(final TestContext context) {
        final Consumer<Handler<AsyncResult<String>>> task = mock(Consumer.class);
        final AtomicInteger counter = new AtomicInteger(0);
        final Async async = context.async();

        doAnswer(invocation -> {
            final Handler<AsyncResult<String>> handler = invocation.getArgumentAt(0, Handler.class);
            if (counter.incrementAndGet() < 10) {
                handler.handle(DefaultAsyncResult.fail(new RuntimeException("Failed")));
            } else {
                handler.handle(DefaultAsyncResult.succeed("TASK 1"));
            }
            return null;
        }).when(task).accept(any(Handler.class));

        AsyncFactorySingleton.getInstance().createFlows(rule.vertx().getOrCreateContext()).retry(options, task, result -> {
            verify(task, times(10)).accept(any(Handler.class));
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals("TASK 1", result.result());
            async.complete();
        });
    }

}
