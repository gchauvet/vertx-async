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
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zatarox.vertx.async.fakes.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public final class FlowsAsyncTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test(timeout = 100)
    public void seriesStillExecutesWhenThereAreNoTasks(TestContext context) {
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.asList(), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final List<Object> resultList = result.result();
            context.assertNotNull(resultList);
            context.assertTrue(resultList.isEmpty());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void seriesExecutesOneTask(TestContext context) {
        final FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.asList(task1), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final List<Object> resultList = result.result();
            context.assertNotNull(resultList);
            context.assertTrue(resultList.containsAll(Arrays.asList(task1.result())));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void seriesExecutesTwoTasks(TestContext context) {
        final FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.asList(task1, task2), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, task1.runCount());
            context.assertEquals(1, task2.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final List<Object> resultList = result.result();
            context.assertNotNull(resultList);
            context.assertTrue(resultList.containsAll(Arrays.asList(task1.result(), task2.result())));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void seriesFailsWhenATaskFails(TestContext context) {
        final FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.asList(task1), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void seriesExecutesNoMoreTasksWhenATaskFails(TestContext context) {
        final FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.series(rule.vertx(), Arrays.asList(task1, task2), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, (int) task1.runCount());
            context.assertEquals(0, (int) task2.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void retryExecutesTheTask(TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.retry(rule.vertx(), task1, 1, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void retryExecutesTheTaskAgainAfterAFailure(TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>(1, new Throwable("Failed"), "Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.retry(rule.vertx(), task1, 1, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(2, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void retryExecutesTheTaskAgainAfterASecondFailure(TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>(2, new Throwable("Failed"), "Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.retry(rule.vertx(), task1, 2, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(3, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void retryFailsAfterTheRetryTimes(TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.retry(rule.vertx(), task1, 1, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(2, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void foreverExecutesTheTaskUntilItFails(TestContext context) {
        final FakeFailingAsyncSupplier<Void> task1 = new FakeFailingAsyncSupplier<>(2, null, new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        FlowsAsync.forever(rule.vertx(), task1, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(3, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            final Object resultValue = result.result();
            context.assertNull(resultValue);
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void waterfallOneTask(TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final List<BiConsumer<Object, Handler<AsyncResult<Object>>>> functions = new LinkedList<>();
        final Async async = context.async();

        functions.add((BiConsumer) task1);

        FlowsAsync.waterfall(rule.vertx(), functions, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = (String) result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void waterfallTwoTasks(TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final List<BiConsumer<Object, Handler<AsyncResult<Object>>>> functions = new LinkedList<>();
        final Async async = context.async();

        functions.add((BiConsumer) task1);
        functions.add((BiConsumer) task2);

        FlowsAsync.waterfall(rule.vertx(), functions, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, task1.runCount());
            context.assertEquals(task1.result(), task2.consumedValue());
            context.assertEquals(1, task2.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final Integer resultValue = (Integer) result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task2.result(), resultValue);
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void waterfallFailsWhenATaskFails(TestContext context) {
        final FakeFailingAsyncFunction<Void, String> task1 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final List<BiConsumer<Object, Handler<AsyncResult<Object>>>> functions = new LinkedList<>();
        final Async async = context.async();

        functions.add((BiConsumer) task1);

        FlowsAsync.waterfall(rule.vertx(), functions, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, (int) task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void waterfallNoMoreTasksWhenATaskFails(TestContext context) {
        final FakeFailingAsyncFunction<Void, String> task1 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final List<BiConsumer<Object, Handler<AsyncResult<Object>>>> functions = new LinkedList<>();
        final Async async = context.async();

        functions.add((BiConsumer) task1);
        functions.add((BiConsumer) task2);

        FlowsAsync.waterfall(rule.vertx(), functions, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, (int) task1.runCount());
            context.assertEquals(0, task2.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void waterfallFailsWhenAConsumerTaskFails(TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final List<BiConsumer<Object, Handler<AsyncResult<Object>>>> functions = new LinkedList<>();
        final Async async = context.async();

        functions.add((BiConsumer) task1);
        functions.add((BiConsumer) task2);

        FlowsAsync.waterfall(rule.vertx(), functions, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, (int) task1.runCount());
            context.assertEquals(task1.result(), task2.consumedValue());
            context.assertEquals(1, task2.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task2.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void waterfallExecutesNoMoreTasksWhenAConsumerTaskFails(TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final FakeSuccessfulAsyncFunction<Integer, String> task3 = new FakeSuccessfulAsyncFunction<>("Task 3");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final List<BiConsumer<Object, Handler<AsyncResult<Object>>>> functions = new LinkedList<>();
        final Async async = context.async();

        functions.add((BiConsumer) task1);
        functions.add((BiConsumer) task2);
        functions.add((BiConsumer) task3);

        FlowsAsync.waterfall(rule.vertx(), functions, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertEquals(1, (int) task1.runCount());
            context.assertEquals(task1.result(), task2.consumedValue());
            context.assertEquals(1, task2.runCount());
            context.assertEquals(0, task3.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task2.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

}
