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
package io.zatarox.vertx.async;

import io.zatarox.vertx.async.utils.DefaultAsyncResult;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.javatuples.KeyValue;
import org.javatuples.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(VertxUnitRunner.class)
public final class AsyncCollectionsTest {

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
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test(expected = InvocationTargetException.class)
    public void testPrivateConstructor() throws Exception {
        final Constructor<AsyncCollections> c = AsyncCollections.class.getDeclaredConstructor();
        c.setAccessible(true);
        c.newInstance();
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachStillExecutesWhenThereAreNoItems(final TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(0, each.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachExecutesForOneItem(final TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());

            context.assertEquals(1, each.runCount());
            context.assertTrue(each.consumedValues().containsAll(Arrays.asList("One")));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachExecutesForTwoItems(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());

            context.assertEquals(2, each.runCount());
            context.assertTrue(each.consumedValues().containsAll(Arrays.asList("One", "Two")));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachFailsWhenAnItemFails(final TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(each.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, each.runCount());
            context.assertTrue(each.consumedValues().containsAll(items));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachFailsWhenAnExceptionRaised(final TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(each.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, each.runCount());
            context.assertTrue(each.consumedValues().containsAll(items));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachFailsNoMoreThanOnce(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger resultCount = new AtomicInteger(0);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(each.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, resultCount.incrementAndGet());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachOfStillExecutesWhenThereAreNoItems(final TestContext context) {
        final Map<String, Void> items = new HashMap<>();
        final FakeFailingAsyncFunction<KeyValue<String, Void>, Void> each = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(0, each.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachOfExecutesForOneItem(final TestContext context) {
        final Map<String, Integer> items = new HashMap<>();
        final FakeSuccessfulAsyncFunction<KeyValue<String, Integer>, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        items.put("One", 1);

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());

            context.assertEquals(1, each.runCount());
            each.consumedValues().stream().forEach((item) -> {
                context.assertEquals(item.getValue(), items.get(item.getKey()));
            });
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachOfExecutesForTwoItems(final TestContext context) {
        final Map<String, Integer> items = new HashMap<>();
        final FakeSuccessfulAsyncFunction<KeyValue<String, Integer>, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        items.put("One", 1);
        items.put("Two", 2);

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());

            context.assertEquals(2, each.runCount());
            each.consumedValues().stream().forEach((item) -> {
                context.assertEquals(item.getValue(), items.get(item.getKey()));
            });
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachOfFailsWhenAnItemFails(final TestContext context) {
        final Map<String, Integer> items = new HashMap<>();
        final FakeFailingAsyncFunction<KeyValue<String, Integer>, Void> each = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        items.put("One", 1);

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(each.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, each.runCount());
            each.consumedValues().stream().forEach((item) -> {
                context.assertEquals(item.getValue(), items.get(item.getKey()));
            });
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void eachOfFailsNoMoreThanOnce(final TestContext context) {
        final Map<String, Integer> items = new HashMap<>();
        final FakeFailingAsyncFunction<KeyValue<String, Integer>, Void> each = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger resultCount = new AtomicInteger(0);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        items.put("One", 1);
        items.put("Two", 2);

        AsyncCollections.each(items, each, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(each.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, resultCount.incrementAndGet());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void mapCollectionWhenThereAreNoItems(final TestContext context) {
        final List<Integer> items = Arrays.asList();
        final FakeAsyncFunction<Integer, Integer> each = new FakeAsyncFunction<Integer, Integer>() {
            @Override
            public void accept(Integer t, Handler<AsyncResult<Integer>> u) {
                u.handle(DefaultAsyncResult.succeed(t * t));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.map(items, each, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, each.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void mapCollectionInFail(final TestContext context) {
        final List<Integer> items = Arrays.asList(1, 2, 3);
        final FakeFailingAsyncFunction<Integer, Integer> each = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.map(items, each, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, each.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void mapCollectionExceptionRaised(final TestContext context) {
        final List<Integer> items = Arrays.asList(1, 2, 3);
        final FakeFailingAsyncFunction<Integer, Integer> each = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.map(items, each, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, each.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void mapStillExecutesWhenThereAreThreeItems(final TestContext context) {
        final List<Integer> items = Arrays.asList(1, 3, 10);
        final FakeAsyncFunction<Integer, Integer> each = new FakeAsyncFunction<Integer, Integer>() {
            @Override
            public void accept(Integer t, Handler<AsyncResult<Integer>> u) {
                incrementRunCount();
                u.handle(DefaultAsyncResult.succeed(t * t));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.map(items, each, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, each.runCount());
            context.assertEquals(3, result.result().size());
            context.assertTrue(result.result().containsAll(Arrays.asList(1 * 1, 3 * 3, 10 * 10)));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void filterStillExecutesWhenThereAreNoItems(final TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, filter.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void filterExecutesForOneItem(final TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed("Two".equals(t)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());

            context.assertEquals(1, filter.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void filterExecutesForTwoItems(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed("Two".equals(t)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(1 == result.result().size());
            context.assertTrue(result.result().containsAll(Arrays.asList("Two")));

            context.assertEquals(2, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two")));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void filterFailsWhenAnItemFails(final TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(items));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void filterFailsWhenAnExceptionRaised(final TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(items));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void filterRejectAllItems(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(false));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());

            context.assertEquals(3, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two", "Three")));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void filterAcceptAllItems(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(true));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, result.result().size());

            context.assertEquals(3, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two", "Three")));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void filterFailsNoMoreThanOnce(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger resultCount = new AtomicInteger(0);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, resultCount.incrementAndGet());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void rejectStillExecutesWhenThereAreNoItems(final TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reject(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, filter.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void rejectExecutesForOneItem(final TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed("One".equals(t)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reject(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());

            context.assertEquals(1, filter.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void rejectExecutesForTwoItems(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed("One".equals(t)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reject(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(1 == result.result().size());
            context.assertTrue(result.result().containsAll(Arrays.asList("Two")));

            context.assertEquals(2, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two")));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void rejectFailsWhenAnItemFails(final TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(items));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void rejectFailsWhenAnExceptionRaised(final TestContext context) {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.filter(items, filter, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(items));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void rejectNoItems(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(false));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reject(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, result.result().size());

            context.assertEquals(3, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two", "Three")));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void rejectKeepAllItems(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Boolean> filter = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String t, Handler<AsyncResult<Boolean>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(false));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reject(items, filter, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, result.result().size());

            context.assertEquals(3, filter.runCount());
            context.assertTrue(filter.consumedValues().containsAll(Arrays.asList("One", "Two", "Three")));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void rejectFailsNoMoreThanOnce(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger resultCount = new AtomicInteger(0);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reject(items, filter, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, resultCount.incrementAndGet());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void rejectNoMoreThanOnceExceptionRaised(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeFailingAsyncFunction<String, Boolean> filter = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"), false);
        final AtomicInteger resultCount = new AtomicInteger(0);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reject(items, filter, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(filter.cause(), result.cause());
            context.assertNull(result.result());

            context.assertEquals(1, resultCount.incrementAndGet());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void transformCollectionStillExecutesWhenThereAreNoItems(final TestContext context) {
        final List<Integer> items = Arrays.asList();
        final FakeAsyncFunction<Integer, String> mapper = new FakeAsyncFunction<Integer, String>() {
            @Override
            public void accept(Integer t, Handler<AsyncResult<String>> u) {
                incrementRunCount();
                consumedValues().add(t);
                u.handle(DefaultAsyncResult.succeed(Integer.toString(t * t)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.transform(items, mapper, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, mapper.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void transformCollectionStillExecutesWhenThereAreThreeItems(final TestContext context) {
        final List<Integer> items = Arrays.asList(1, 3, 10);
        final FakeAsyncFunction<Integer, String> mapper = new FakeAsyncFunction<Integer, String>() {
            @Override
            public void accept(Integer t, Handler<AsyncResult<String>> u) {
                incrementRunCount();
                u.handle(DefaultAsyncResult.succeed(Integer.toString(t * t)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.transform(items, mapper, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, mapper.runCount());
            context.assertEquals(3, result.result().size());
            context.assertTrue(result.result().containsAll(Arrays.asList(Integer.toString(1 * 1), Integer.toString(3 * 3), Integer.toString(10 * 10))));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void transformCollectionFails(final TestContext context) {
        final List<Integer> items = Arrays.asList(1, 3, 10);
        final FakeFailingAsyncFunction<Integer, String> mapper = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.transform(items, mapper, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertTrue(result.failed());
            context.assertEquals(1, mapper.runCount());
            context.assertTrue(result.cause() instanceof Throwable);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void transformCollectionExceptionRaised(final TestContext context) {
        final List<Integer> items = Arrays.asList(1, 3, 10);
        final FakeFailingAsyncFunction<Integer, String> mapper = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.transform(items, mapper, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertTrue(result.failed());
            context.assertEquals(1, mapper.runCount());
            context.assertTrue(result.cause() instanceof Throwable);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void transformMapFails(final TestContext context) {
        final Map<Integer, String> items = new HashMap<>();
        final FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>> mapper = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        items.put(1, "One");

        AsyncCollections.transform(items, mapper, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertTrue(result.failed());
            context.assertNull(result.result());
            context.assertEquals(1, mapper.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void transformMapExceptionRaise(final TestContext context) {
        final Map<Integer, String> items = new HashMap<>();
        final FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>> mapper = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();
        items.put(1, "One");

        AsyncCollections.transform(items, mapper, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertTrue(result.failed());
            context.assertNull(result.result());
            context.assertEquals(1, mapper.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void transformMapStillExecutesWhenThereAreNoItems(final TestContext context) {
        final Map<Integer, String> items = new HashMap<>();
        final FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>> mapper = new FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>>() {
            @Override
            public void accept(KeyValue<Integer, String> in, Handler<AsyncResult<KeyValue<String, Integer>>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(new KeyValue<>(in.getValue(), in.getKey())));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.transform(items, mapper, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, mapper.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void transformMapStillExecutesWhenThereAreThreeItems(final TestContext context) {
        final Map<Integer, String> items = new HashMap<>();
        final FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>> mapper = new FakeAsyncFunction<KeyValue<Integer, String>, KeyValue<String, Integer>>() {
            @Override
            public void accept(KeyValue<Integer, String> in, Handler<AsyncResult<KeyValue<String, Integer>>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(new KeyValue<>(in.getValue(), in.getKey())));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        items.put(0, "Zero");
        items.put(1, "One");
        items.put(2, "Two");

        AsyncCollections.transform(items, mapper, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(3, mapper.runCount());
            context.assertEquals(3, result.result().size());
            context.assertEquals(0, result.result().get("Zero"));
            context.assertEquals(1, result.result().get("One"));
            context.assertEquals(2, result.result().get("Two"));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void reduceWhenThereAreNoItems(final TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeAsyncFunction<Pair<String, Integer>, Integer>() {
            @Override
            public void accept(Pair<String, Integer> in, Handler<AsyncResult<Integer>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(Integer.valueOf(in.getValue0()) + in.getValue1()));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reduce(items, 0, reducer, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(0, result.result());
            context.assertEquals(0, reducer.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void reduceWhenThereAreItems(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeAsyncFunction<Pair<String, Integer>, Integer>() {
            @Override
            public void accept(Pair<String, Integer> in, Handler<AsyncResult<Integer>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(Integer.valueOf(in.getValue0()) + in.getValue1()));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reduce(items, 0, reducer, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(6, result.result());
            context.assertEquals(3, reducer.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void reduceWhenThereAreAnItemFails(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reduce(items, 0, reducer, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, reducer.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void reduceWhenThereAreAnItemExceptionRaise(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reduce(items, 0, reducer, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, reducer.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void reduceWhenThereAreLastItemFails(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), true);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reduce(items, 0, reducer, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(3, reducer.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void reduceWhenThereAreLastItemExceptionRaise(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<Pair<String, Integer>, Integer> reducer = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.reduce(items, 0, reducer, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(3, reducer.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void detectWhenThereAreNoItems(final TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(!"".equalsIgnoreCase(in)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.detect(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(0, tester.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void detectAnItem(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed("2".equalsIgnoreCase(in)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.detect(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals("2", result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void detectNoItem(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed("".equalsIgnoreCase(in)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.detect(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void detectWithAFailed(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<String, Boolean> tester = new FakeFailingAsyncFunction<>(2, false, new RuntimeException("Failed"), true);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.detect(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.cause() instanceof Throwable);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void detectWithAExceptionRaised(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<String, Boolean> tester = new FakeFailingAsyncFunction<>(2, false, new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.detect(items, tester, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertTrue(result.cause() instanceof RuntimeException);
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void someWhenThereAreNoItems(final TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(!"".equalsIgnoreCase(in)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.some(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertFalse(result.result());
            context.assertEquals(0, tester.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void someAnItem(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed("2".equalsIgnoreCase(in)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.some(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void someNoItem(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed("".equalsIgnoreCase(in)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.some(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertFalse(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void someWithAFailed(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<String, Boolean> tester = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), true);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.some(items, tester, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void someWithAExceptionRaise(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<String, Boolean> tester = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.some(items, tester, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void everyWhenThereAreNoItems(final TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(!"".equalsIgnoreCase(in)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.every(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertFalse(result.result());
            context.assertEquals(0, tester.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void everyAllItem(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(!"".equalsIgnoreCase(in)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.every(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void everyNoAllItem(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeAsyncFunction<String, Boolean> tester = new FakeAsyncFunction<String, Boolean>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Boolean>> out) {
                incrementRunCount();
                consumedValues().add(in);
                out.handle(DefaultAsyncResult.succeed(!"2".equalsIgnoreCase(in)));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.every(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertFalse(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void everyWithAFailed(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<String, Boolean> tester = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), true);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.every(items, tester, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void everyWithAExceptionRaised(final TestContext context) {
        final List<String> items = Arrays.asList("1", "2", "3");
        final FakeFailingAsyncFunction<String, Boolean> tester = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.every(items, tester, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void concatWhenThereAreNoItems(final TestContext context) {
        final List<String> items = Arrays.asList();
        final FakeAsyncFunction<String, Collection<Boolean>> tester = new FakeAsyncFunction<String, Collection<Boolean>>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Collection<Boolean>>> out) {
                incrementRunCount();
                consumedValues().add(in);
                final Collection<Boolean> result = new ArrayList<>(in.length());
                for (char c : in.toCharArray()) {
                    result.add("aeiouy".contains(Character.toString(c)));
                }
                out.handle(DefaultAsyncResult.succeed(result));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.concat(items, tester, result -> {

            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, tester.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void concatAllItems(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Collection<Boolean>> tester = new FakeAsyncFunction<String, Collection<Boolean>>() {
            @Override
            public void accept(String in, Handler<AsyncResult<Collection<Boolean>>> out) {
                incrementRunCount();
                consumedValues().add(in);
                final Collection<Boolean> result = new ArrayList<>(in.length());
                for (char c : in.toCharArray()) {
                    result.add("aeiouy".contains(Character.toString(c)));
                }
                out.handle(DefaultAsyncResult.succeed(result));
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.concat(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(11, result.result().size());
            context.assertEquals(3, tester.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void concatFailed(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Collection<Boolean>> tester = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), true);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.concat(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.failed());
            context.assertTrue(result.cause() instanceof Throwable);
            context.assertNull(result.result());
            context.assertEquals(3, tester.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void concatExceptionRaised(final TestContext context) {
        final List<String> items = Arrays.asList("One", "Two", "Three");
        final FakeAsyncFunction<String, Collection<Boolean>> tester = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncCollections.concat(items, tester, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.failed());
            context.assertTrue(result.cause() instanceof RuntimeException);
            context.assertNull(result.result());
            context.assertEquals(3, tester.runCount());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void sortNoItems(final TestContext context) {
        final List<Integer> items = Arrays.asList();
        final Async async = context.async();
        AsyncCollections.sort(items, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.failed());
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void sortItems(final TestContext context) {
        final List<Integer> items = Arrays.asList(3, 2, 1);
        final Async async = context.async();
        AsyncCollections.sort(items, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.failed());
            context.assertTrue(result.succeeded());
            context.assertEquals(Arrays.asList(1, 2, 3), result.result());
            async.complete();
        });
    }

    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void sortItemsWithValidator(final TestContext context) {
        final List<Integer> items = Arrays.asList(2, 3, 1);
        final Async async = context.async();
        AsyncCollections.sort(items, (a, b) -> b.compareTo(a), result -> {
            context.assertNotNull(result);
            context.assertFalse(result.failed());
            context.assertTrue(result.succeeded());
            context.assertEquals(Arrays.asList(3, 2, 1), result.result());
            async.complete();
        });
    }
    
    @Test(timeout = AsyncCollectionsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncCollectionsTest.REPEAT_LIMIT)
    public void sortItemsWithValidatorExceptionRaised(final TestContext context) {
        final List<Integer> items = Arrays.asList(2, 3, 1);
        final Async async = context.async();
        AsyncCollections.sort(items, (a, b) -> { throw new RuntimeException(); }, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.failed());
            context.assertTrue(result.cause() instanceof RuntimeException);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            async.complete();
        });
    }
}
