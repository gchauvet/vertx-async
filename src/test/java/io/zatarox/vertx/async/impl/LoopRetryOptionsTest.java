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
import io.zatarox.vertx.async.fakes.FakeFailingAsyncSupplier;
import io.zatarox.vertx.async.fakes.FakeSuccessfulAsyncSupplier;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
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
    public MockitoRule mockitoRule = MockitoJUnit.rule();

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
    @Repeat(LoopRetryOptionsTest.REPEAT_LIMIT)
    public void retryExecutesTheTaskWithoutError(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.retry(options, task1, result -> {
            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = LoopRetryOptionsTest.TIMEOUT_LIMIT)
    @Repeat(LoopRetryOptionsTest.REPEAT_LIMIT)
    public void retryExecutesAndFaildOnAllIteratee(final TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.retry(options, task1, result -> {
            context.assertEquals(10, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = LoopRetryOptionsTest.TIMEOUT_LIMIT)
    @Repeat(LoopRetryOptionsTest.REPEAT_LIMIT)
    public void retryExecutesSuccessBeforeLastFailure(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>(9, new Throwable("Failed"), "Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.retry(options, task1, result -> {
            context.assertEquals(10, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

}
