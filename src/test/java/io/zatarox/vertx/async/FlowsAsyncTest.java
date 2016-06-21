/*
 * The MIT License
 *
 * Copyright 2016 Guillaume Chauvet.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.zatarox.vertx.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zatarox.vertx.async.fakes.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public final class FlowsAsyncTest {

    /**
     * Limits
     */
    private static final int TIMEOUT_LIMIT = 1000;
    private static final int REPEAT_LIMIT = 100;

    @Rule
    public RepeatRule repeater = new RepeatRule();
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test(expected = InvocationTargetException.class)
    public void testPrivateConstructor() throws Exception {
        final Constructor<FlowsAsync> c = FlowsAsync.class.getDeclaredConstructor();
        c.setAccessible(true);
        c.newInstance();
    }
    
    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void seriesStillExecutesWhenThereAreNoTasks(final TestContext context) {
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.<Consumer<Handler<AsyncResult<Void>>>>asList(), result -> {

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNotNull(result.result());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void seriesExecutesOneTask(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1), result -> {

            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNotNull(result.result());
            context.assertTrue(result.result().containsAll(Arrays.asList(task1.result())));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void seriesExecutesTwoTasks(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final FakeSuccessfulAsyncSupplier<String> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1, task2), result -> {

            context.assertEquals(1, task1.runCount());
            context.assertEquals(1, task2.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNotNull(result.result());
            context.assertTrue(result.result().containsAll(Arrays.asList(task1.result(), task2.result())));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void seriesFailsWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1), result -> {

            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void seriesExecutesNoMoreTasksWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final FakeSuccessfulAsyncSupplier<String> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1, task2), result -> {

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, (int) task1.runCount());
            context.assertEquals(0, (int) task2.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void retryExecutesTheTask(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.retry(rule.vertx(), task1, 1, result -> {

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

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void retryExecutesTheTaskAgainAfterAFailure(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>(1, new Throwable("Failed"), "Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.retry(rule.vertx(), task1, 1, result -> {

            context.assertEquals(2, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void retryExecutesTheTaskAgainAfterASecondFailure(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>(2, new Throwable("Failed"), "Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.retry(rule.vertx(), task1, 2, result -> {

            context.assertEquals(3, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void retryFailsAfterTheRetryTimes(final TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.retry(rule.vertx(), task1, 1, result -> {

            context.assertEquals(2, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void foreverExecutesTheTaskUntilItFails(final TestContext context) {
        final FakeFailingAsyncSupplier<Void> task1 = new FakeFailingAsyncSupplier<>(2, null, new Throwable("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.forever(rule.vertx(), task1, result -> {

            context.assertEquals(3, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            final Object resultValue = result.result();
            context.assertNull(resultValue);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void waterfallOneTask(final TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.waterfall(rule.vertx(), Arrays.<BiConsumer<Void, Handler<AsyncResult<String>>>>asList(task1), result -> {

            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = (String) result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void waterfallTwoTasks(final TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.waterfall(rule.vertx(), Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1, (BiConsumer) task2), result -> {

            context.assertEquals(1, task1.runCount());
            context.assertEquals(task1.result(), task2.consumedValue());
            context.assertEquals(1, task2.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final Integer resultValue = (Integer) result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task2.result(), resultValue);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void waterfallFailsWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncFunction<Void, String> task1 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.waterfall(rule.vertx(), Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1), result -> {

            context.assertEquals(1, (int) task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void waterfallNoMoreTasksWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncFunction<Void, String> task1 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.waterfall(rule.vertx(), Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1, (BiConsumer) task2), result -> {

            context.assertEquals(1, (int) task1.runCount());
            context.assertEquals(0, task2.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void waterfallFailsWhenAConsumerTaskFails(final TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.waterfall(rule.vertx(), Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1, (BiConsumer) task2), result -> {

            context.assertEquals(1, (int) task1.runCount());
            context.assertEquals(task1.result(), task2.consumedValue());
            context.assertEquals(1, task2.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task2.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = FlowsAsyncTest.TIMEOUT_LIMIT)
    @Repeat(FlowsAsyncTest.REPEAT_LIMIT)
    public void waterfallExecutesNoMoreTasksWhenAConsumerTaskFails(final TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final FakeSuccessfulAsyncFunction<Integer, String> task3 = new FakeSuccessfulAsyncFunction<>("Task 3");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        FlowsAsync.waterfall(rule.vertx(), Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1, (BiConsumer) task2, (BiConsumer) task3), result -> {

            context.assertEquals(1, (int) task1.runCount());
            context.assertEquals(task1.result(), task2.consumedValue());
            context.assertEquals(1, task2.runCount());
            context.assertEquals(0, task3.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task2.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

}
