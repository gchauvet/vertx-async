/*
 * The MIT License
 *
 * Copyright 2016 Guillaume.
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
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public final class CollectionsAsyncTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    
    @Test(timeout = 100)
    public void itStillExecutesWhenThereAreNoItems(TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.each(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(0, each.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void itExecutesForOneItem(TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.each(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());

            context.assertEquals(1, each.runCount());
            context.assertTrue(each.consumedValues().containsAll(Arrays.asList("One")));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void itExecutesForTwoItems(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.each(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());

            context.assertEquals(2, each.runCount());
            context.assertTrue(each.consumedValues().containsAll(Arrays.asList("One", "Two")));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void itFailsWhenAnItemFails(TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.each(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(each.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, each.runCount());
            context.assertTrue(each.consumedValues().containsAll(items));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void itFailsNoMoreThanOnce(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> resultCount = new ObjectWrapper<>(0);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.each(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(each.cause(), result.cause());
            context.assertNull(result.result());

            resultCount.setObject(resultCount.getObject() + 1);
            context.assertEquals(1, resultCount.getObject());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = 100)
    public void itStillExecutesWhenThereAreNoTasks(TestContext context) {
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.series(rule.vertx(), Arrays.asList(), result -> {
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
    public void itExecutesOneTask(TestContext context) {
        final FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.series(rule.vertx(), Arrays.asList(task1), result -> {
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
    public void itExecutesTwoTasks(TestContext context) {
        final FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.series(rule.vertx(), Arrays.asList(task1, task2), result -> {
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
    public void itFailsWhenATaskFails(TestContext context) {
        final FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.series(rule.vertx(), Arrays.asList(task1), result -> {
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
    public void itExecutesNoMoreTasksWhenATaskFails(TestContext context) {
        final FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.series(rule.vertx(), Arrays.asList(task1, task2), result -> {
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
    public void itStillExecutesWhenThereAreNoItemsToMap(TestContext context) {
        final List<Integer> items = Arrays.asList();
        final FakeAsyncFunction<Integer, Integer> each = new FakeAsyncFunction<Integer, Integer>() {
            @Override
            public void accept(Integer t, Handler<AsyncResult<Integer>> u) {
                u.handle(DefaultAsyncResult.succeed(t * t));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.map(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, each.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = 100)
    public void itStillExecutesWhenThereAreThreeItemsToMap(TestContext context) {
        final List<Integer> items = Arrays.asList(1, 3, 10);
        final FakeAsyncFunction<Integer, Integer> each = new FakeAsyncFunction<Integer, Integer>() {
            @Override
            public void accept(Integer t, Handler<AsyncResult<Integer>> u) {
                incrementRunCount();
                u.handle(DefaultAsyncResult.succeed(t * t));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();
        
        CollectionsAsync.map(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);
            
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, each.runCount());
            context.assertEquals(3, result.result().size());
            context.assertTrue(result.result().containsAll(Arrays.asList(1 * 1, 3 * 3, 10 * 10)));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
}
