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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.javatuples.KeyValue;
import org.javatuples.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public final class CollectionsAsyncTest {

    /**
     * Timelimit
     */
    private static final int LIMIT = 1000;
    
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void eachStillExecutesWhenThereAreNoItems(TestContext context) {
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

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void eachExecutesForOneItem(TestContext context) {
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

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void eachExecutesForTwoItems(TestContext context) {
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

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void eachFailsWhenAnItemFails(TestContext context) {
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

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void eachFailsNoMoreThanOnce(TestContext context) {
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
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void forEachOfStillExecutesWhenThereAreNoItems(TestContext context) {
        final Map<String, Void> items = new HashMap<>();
        final FakeFailingAsyncFunction<KeyValue<String, Void>, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.forEachOf(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(0, each.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void forEachOfExecutesForOneItem(TestContext context) {
        final Map<String, Integer> items = new HashMap<>();
        final FakeSuccessfulAsyncFunction<KeyValue<String, Integer>, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();
        items.put("One", 1);

        CollectionsAsync.forEachOf(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());

            context.assertEquals(1, each.runCount());
            each.consumedValues().stream().forEach((item) -> {
                context.assertEquals(item.getValue(), items.get(item.getKey()));
            });
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void forEachOfExecutesForTwoItems(TestContext context) {
        final Map<String, Integer> items = new HashMap<>();
        final FakeSuccessfulAsyncFunction<KeyValue<String, Integer>, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();
        items.put("One", 1);
        items.put("Two", 2);
        
        CollectionsAsync.forEachOf(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());

            context.assertEquals(2, each.runCount());
            each.consumedValues().stream().forEach((item) -> {
                context.assertEquals(item.getValue(), items.get(item.getKey()));
            });
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void forEachOfFailsWhenAnItemFails(TestContext context) {
        final Map<String, Integer> items = new HashMap<>();
        final FakeFailingAsyncFunction<KeyValue<String, Integer>, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();
        items.put("One", 1);

        CollectionsAsync.forEachOf(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(each.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, each.runCount());
            each.consumedValues().stream().forEach((item) -> {
                context.assertEquals(item.getValue(), items.get(item.getKey()));
            });
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void forEachOfFailsNoMoreThanOnce(TestContext context) {
        final Map<String, Integer> items = new HashMap<>();
        final FakeFailingAsyncFunction<KeyValue<String, Integer>, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> resultCount = new ObjectWrapper<>(0);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();
        items.put("One", 1);
        items.put("Two", 2);
        
        CollectionsAsync.forEachOf(rule.vertx(), items, each, result -> {
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

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void mapStillExecutesWhenThereAreNoItemsToMap(TestContext context) {
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

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void mapStillExecutesWhenThereAreThreeItemsToMap(TestContext context) {
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

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void filterStillExecutesWhenThereAreNoItems(TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.filter(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, filter.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void filterExecutesForOneItem(TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed("Two".equals(t)));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.filter(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());

            context.assertEquals(1, filter.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void filterExecutesForTwoItems(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed("Two".equals(t)));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.filter(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(1 == result.result().size());
            context.assertTrue(result.result().containsAll(Arrays.asList("Two")));

            context.assertEquals(2, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two")));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void filterFailsWhenAnItemFails(TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.filter(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(items));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void filterRejectAllItems(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(false));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.filter(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());

            context.assertEquals(3, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two", "Three")));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void filterAcceptAllItems(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(true));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.filter(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, result.result().size());

            context.assertEquals(3, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two", "Three")));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void filterFailsNoMoreThanOnce(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> resultCount = new ObjectWrapper<>(0);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.filter(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            resultCount.setObject(resultCount.getObject() + 1);
            context.assertEquals(1, resultCount.getObject());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void rejectStillExecutesWhenThereAreNoItems(TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reject(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, filter.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void rejectExecutesForOneItem(TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed("One".equals(t)));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reject(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());

            context.assertEquals(1, filter.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void rejectExecutesForTwoItems(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed("One".equals(t)));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reject(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(1 == result.result().size());
            context.assertTrue(result.result().containsAll(Arrays.asList("Two")));

            context.assertEquals(2, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two")));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void rejectFailsWhenAnItemFails(TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.filter(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(items));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void rejectNoItems(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(false));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reject(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, result.result().size());

            context.assertEquals(3, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two", "Three")));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void rejectKeepAllItems(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(false));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reject(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, result.result().size());

            context.assertEquals(3, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two", "Three")));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void rejectFailsNoMoreThanOnce(TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> resultCount = new ObjectWrapper<>(0);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reject(rule.vertx(), items, filter, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            resultCount.setObject(resultCount.getObject() + 1);
            context.assertEquals(1, resultCount.getObject());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void transformCollectionStillExecutesWhenThereAreNoItemsToMap(TestContext context) {
        final List<Integer> items = Arrays.asList();
        final FakeAsyncFunction<Integer, String> each = new FakeAsyncFunction<Integer, String>() {
            @Override
            public void accept(Integer t, Handler<AsyncResult<String>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(Integer.toString(t * t)));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.transform(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, each.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void transformCollectionStillExecutesWhenThereAreThreeItemsToMap(TestContext context) {
        final List<Integer> items = Arrays.asList(1, 3, 10);
        final FakeAsyncFunction<Integer, String> each = new FakeAsyncFunction<Integer, String>() {
            @Override
            public void accept(Integer t, Handler<AsyncResult<String>> u) {
                incrementRunCount();
                u.handle(DefaultAsyncResult.succeed(Integer.toString(t * t)));
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
            context.assertTrue(result.result().containsAll(Arrays.asList(Integer.toString(1 * 1), Integer.toString(3 * 3), Integer.toString(10 * 10))));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void transformMapStillExecutesWhenThereAreNoItemsToMap(TestContext context) {
        final Map<Integer, String> items = new HashMap<>();
        final FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>> each = new FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>>() {
            @Override
            public void accept(KeyValue<Integer, String> in, Handler<AsyncResult<KeyValue<String, Integer>>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(new KeyValue<>(in.getValue(), in.getKey())));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.transform(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, each.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }

    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void transformMapStillExecutesWhenThereAreThreeItemsToMap(TestContext context) {
        final Map<Integer, String> items = new HashMap<>();
        final FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>> each = new FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>>() {
            @Override
            public void accept(KeyValue<Integer, String> in, Handler<AsyncResult<KeyValue<String, Integer>>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(new KeyValue<>(in.getValue(), in.getKey())));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();
        
        items.put(0, "Zero");
        items.put(1, "One");
        items.put(2, "Two");

        CollectionsAsync.transform(rule.vertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, each.runCount());
            context.assertEquals(3, result.result().size());
            context.assertEquals(0, result.result().get("Zero"));
            context.assertEquals(1, result.result().get("One"));
            context.assertEquals(2, result.result().get("Two"));
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void reduceWhenThereAreNoItems(TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeAsyncFunction<Pair<String, Integer>, Integer>() {
            @Override
            public void accept(Pair<String, Integer> in, Handler<AsyncResult<Integer>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(Integer.valueOf(in.getValue0()) + in.getValue1()));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reduce(rule.vertx(), items, 0, reducer, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(0, result.result());
            context.assertEquals(0, reducer.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void reduceWhenThereAreItems(TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeAsyncFunction<Pair<String, Integer>, Integer>() {
            @Override
            public void accept(Pair<String, Integer> in, Handler<AsyncResult<Integer>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(Integer.valueOf(in.getValue0()) + in.getValue1()));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reduce(rule.vertx(), items, 0, reducer, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(6, result.result());
            context.assertEquals(3, reducer.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void reduceWhenThereAreAnItemFails(TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reduce(rule.vertx(), items, 0, reducer, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, reducer.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void reduceWhenThereAreLastItemFails(TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeFailingAsyncFunction<>(2, null, new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.reduce(rule.vertx(), items, 0, reducer, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(3, reducer.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void detectWhenThereAreNoItems(TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(!"".equalsIgnoreCase(in)));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.detect(rule.vertx(), items, tester, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(0, tester.runCount());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void detectAnItem(TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed("2".equalsIgnoreCase(in)));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.detect(rule.vertx(), items, tester, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals("2", result.result());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void detectNoItem(TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed("".equalsIgnoreCase(in)));
            }
        };
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.detect(rule.vertx(), items, tester, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
    
    @Test(timeout = CollectionsAsyncTest.LIMIT)
    public void detectWithAFailed(TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<String, Boolean> tester = new FakeFailingAsyncFunction<>(2, null, new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);
        final Async async = context.async();

        CollectionsAsync.detect(rule.vertx(), items, tester, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, (int) handlerCallCount.getObject());
            async.complete();
        });
    }
}
