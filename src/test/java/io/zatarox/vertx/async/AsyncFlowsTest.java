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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(VertxUnitRunner.class)
public final class AsyncFlowsTest {

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
        final Constructor<AsyncFlows> c = AsyncFlows.class.getDeclaredConstructor();
        c.setAccessible(true);
        c.newInstance();
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void seriesStillExecutesWhenThereAreNoTasks(final TestContext context) {
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.series(Arrays.<Consumer<Handler<AsyncResult<Void>>>>asList(), result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNotNull(result.result());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void seriesExecutesOneTask(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.series(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1), result -> {
            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNotNull(result.result());
            context.assertTrue(result.result().containsAll(Arrays.asList(task1.result())));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void seriesExecutesTwoTasks(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final FakeSuccessfulAsyncSupplier<String> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.series(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1, task2), result -> {
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

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void seriesFailsWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.series(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1), result -> {
            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void seriesExecutesNoMoreTasksWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new RuntimeException("Failed"));
        final FakeSuccessfulAsyncSupplier<String> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.series(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1, task2), result -> {
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

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void foreverExecutesTheTaskUntilItFails(final TestContext context) {
        final FakeFailingAsyncSupplier<Void> task1 = new FakeFailingAsyncSupplier<>(2, null, new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.forever(task1, result -> {
            context.assertEquals(3, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            final Object resultValue = result.result();
            context.assertNull(resultValue);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void foreverExecutesWithRaisedException(final TestContext context) {
        final FakeFailingAsyncSupplier<Void> task1 = new FakeFailingAsyncSupplier<>(2, null, new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.forever(task1, result -> {
            context.assertEquals(3, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            final Object resultValue = result.result();
            context.assertNull(resultValue);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void waterfallOneTask(final TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final Async async = context.async();

        AsyncFlows.waterfall(Arrays.<BiConsumer<Void, Handler<AsyncResult<String>>>>asList(task1), result -> {
            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            final String resultValue = (String) result.result();
            context.assertNotNull(resultValue);
            context.assertEquals(task1.result(), resultValue);
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void waterfallTwoTasks(final TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.waterfall(Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1, (BiConsumer) task2), result -> {
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

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void waterfallFailsWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncFunction<Void, String> task1 = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.waterfall(Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1), result -> {
            context.assertEquals(1, (int) task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void waterfallFailsWhenAExceptionTaskRaised(final TestContext context) {
        final Async async = context.async();

        AsyncFlows.waterfall(Arrays.asList((item, handler) -> {
            throw new ClassCastException();
        }), result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertTrue(result.cause() instanceof ClassCastException);
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void waterfallNoMoreTasksWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncFunction<Void, String> task1 = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.waterfall(Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1, (BiConsumer) task2), result -> {
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

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void waterfallFailsWhenAConsumerTaskFails(final TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.waterfall(Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1, (BiConsumer) task2), result -> {
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

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void waterfallExecutesNoMoreTasksWhenAConsumerTaskFails(final TestContext context) {
        final FakeSuccessfulAsyncFunction<Void, String> task1 = new FakeSuccessfulAsyncFunction<>("Task 1");
        final FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new RuntimeException("Failed"));
        final FakeSuccessfulAsyncFunction<Integer, String> task3 = new FakeSuccessfulAsyncFunction<>("Task 3");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.waterfall(Arrays.<BiConsumer<Object, Handler<AsyncResult<Object>>>>asList((BiConsumer) task1, (BiConsumer) task2, (BiConsumer) task3), result -> {
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

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void parallelStillExecutesWhenThereAreNoTasks(final TestContext context) {
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.parallel(Arrays.<Consumer<Handler<AsyncResult<Void>>>>asList(), result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNotNull(result.result());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void parallelExecutesOneTask(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.parallel(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1), result -> {
            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNotNull(result.result());
            context.assertTrue(result.result().containsAll(Arrays.asList(task1.result())));
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void parallelExecutesTwoTasks(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final FakeSuccessfulAsyncSupplier<String> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.parallel(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1, task2), result -> {
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

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void parallelFailsWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new RuntimeException("Failed"));
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.parallel(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1), result -> {
            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void parallelExecutesNoMoreTasksWhenATaskFails(final TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new RuntimeException("Failed"));
        final FakeSuccessfulAsyncSupplier<String> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.parallel(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1, task2), result -> {
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

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void parallelFailsWhenATaskRaisedException(final TestContext context) {
        final FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new RuntimeException("Failed"), false);
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.parallel(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1), result -> {
            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertEquals(task1.cause(), result.cause());
            context.assertNull(result.result());
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void whilstExecutesEmpty(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger();
        final Async async = context.async();
        AsyncFlows.whilst(() -> counter.incrementAndGet() < 1, t -> {
            t.handle(DefaultAsyncResult.fail(new IllegalAccessException()));
        }, e -> {
            context.assertTrue(e.succeeded());
            context.assertEquals(1, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void whilstExecutesMany(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger();
        final Async async = context.async();
        AsyncFlows.whilst(() -> counter.incrementAndGet() < 100, t -> {
            t.handle(DefaultAsyncResult.succeed());
        }, e -> {
            context.assertTrue(e.succeeded());
            context.assertEquals(100, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void whilstExecutesWithFails(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger();
        final Async async = context.async();
        AsyncFlows.whilst(() -> counter.incrementAndGet() < 2, t -> {
            t.handle(DefaultAsyncResult.fail(new IllegalAccessException()));
        }, e -> {
            context.assertFalse(e.succeeded());
            context.assertTrue(e.cause() instanceof IllegalAccessException);
            context.assertEquals(1, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void whilstExecutesWithRaiseException(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger();
        final Async async = context.async();
        AsyncFlows.whilst(() -> counter.incrementAndGet() < 2, t -> {
            throw new IllegalAccessError();
        }, e -> {
            context.assertFalse(e.succeeded());
            context.assertTrue(e.cause() instanceof IllegalAccessError);
            context.assertEquals(1, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void untilExecutesEmpty(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger();
        final Async async = context.async();
        AsyncFlows.until(() -> false, t -> {
            counter.incrementAndGet();
            t.handle(DefaultAsyncResult.succeed());
        }, e -> {
            context.assertTrue(e.succeeded());
            context.assertEquals(1, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void untilExecutesMany(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger();
        final Async async = context.async();
        AsyncFlows.until(() -> counter.incrementAndGet() < 100, t -> {
            t.handle(DefaultAsyncResult.succeed());
        }, e -> {
            context.assertTrue(e.succeeded());
            context.assertEquals(100, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void untilExecutesAndFails(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger();
        final Async async = context.async();
        AsyncFlows.until(() -> counter.incrementAndGet() < 2, t -> {
            t.handle(DefaultAsyncResult.fail(new IllegalAccessException()));
        }, e -> {
            context.assertFalse(e.succeeded());
            context.assertTrue(e.cause() instanceof IllegalAccessException);
            context.assertEquals(0, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void untilExecutesExceptionRaised(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger();
        final Async async = context.async();
        AsyncFlows.until(() -> counter.incrementAndGet() < 2, t -> {
            throw new IllegalAccessError();
        }, e -> {
            context.assertFalse(e.succeeded());
            context.assertTrue(e.cause() instanceof IllegalAccessError);
            context.assertEquals(0, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void seqWithoutFunctionsExecutes(final TestContext context) {
        final Async async = context.async();
        final BiConsumer<Object, Handler<AsyncResult<Void>>> result = AsyncFlows.seq();

        context.assertNotNull(result);
        rule.vertx().runOnContext(e -> {
            result.accept(null, e1 -> {
                context.assertTrue(e1.succeeded());
                async.complete();
            });
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void seqFunctions(final TestContext context) {
        final Async async = context.async();

        final BiConsumer<Integer, Handler<AsyncResult<Integer>>> result = AsyncFlows.seq(
                (t, u) -> {
                    u.handle(DefaultAsyncResult.succeed(t + 1));
                }, (t, u) -> {
                    u.handle(DefaultAsyncResult.succeed(t * 4));
                });

        context.assertNotNull(result);
        rule.vertx().runOnContext(e -> {
            result.accept(3, e1 -> {
                context.assertTrue(e1.succeeded());
                context.assertEquals(16, e1.result());
                async.complete();
            });
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void seqFunctionsWithFails(final TestContext context) {
        final Async async = context.async();

        final BiConsumer<Integer, Handler<AsyncResult<Integer>>> result = AsyncFlows.seq(
                (t, u) -> {
                    u.handle(DefaultAsyncResult.succeed(t + 1));
                }, (t, u) -> {
                    u.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
                });

        context.assertNotNull(result);
        rule.vertx().runOnContext(e -> {
            result.accept(3, e1 -> {
                context.assertFalse(e1.succeeded());
                context.assertTrue(e1.cause() instanceof IllegalArgumentException);
                context.assertNull(e1.result());
                async.complete();
            });
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void seqFunctionsWithExceptionRaised(final TestContext context) {
        final Async async = context.async();

        final BiConsumer<Integer, Handler<AsyncResult<Integer>>> result = AsyncFlows.seq(
                (t, u) -> {
                    u.handle(DefaultAsyncResult.succeed(t + 1));
                }, (t, u) -> {
                    throw new IllegalAccessError();
                });

        context.assertNotNull(result);
        rule.vertx().runOnContext(e -> {
            result.accept(3, e1 -> {
                context.assertFalse(e1.succeeded());
                context.assertTrue(e1.cause() instanceof IllegalAccessError);
                context.assertNull(e1.result());
                async.complete();
            });
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void timesWhenThereAreNoItems(final TestContext context) {
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.times((Integer) 0, (value, handler) -> {
            handlerCallCount.incrementAndGet();
            handler.handle(DefaultAsyncResult.succeed(value.toString()));
        }, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertTrue(result.result().isEmpty());
            context.assertEquals(0, handlerCallCount.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void timesInFail(final TestContext context) {
        final FakeFailingAsyncFunction function = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), true);
        final Async async = context.async();

        AsyncFlows.times(3, function, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(3, function.runCount());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void timesWithExceptionRaised(final TestContext context) {
        final FakeFailingAsyncFunction function = new FakeFailingAsyncFunction<>(2, null, new RuntimeException("Failed"), false);
        final Async async = context.async();

        AsyncFlows.times(3, function, result -> {
            context.assertNotNull(result);
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(3, function.runCount());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void timesWithThreeIteration(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.times(3, (value, handler) -> {
            counter.incrementAndGet();
            handler.handle(DefaultAsyncResult.succeed(value.toString()));
        }, result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(Arrays.asList("0", "1", "2"), result.result());
            context.assertEquals(3, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void raceExecutesEmptyTask(final TestContext context) {
        final Async async = context.async();

        AsyncFlows.race(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(), result -> {
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void raceExecutesOneTask(final TestContext context) {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.race(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1), result -> {
            context.assertEquals(1, task1.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(result.result(), "Task 1");
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void raceExecutesTwoTasks(final TestContext context) {
        final FakeAsyncSupplier<String> task1 = new FakeAsyncSupplier<String>() {
            @Override
            public void accept(Handler<AsyncResult<String>> u) {
                rule.vertx().setTimer(200, id -> {
                    incrementRunCount();
                    u.handle(DefaultAsyncResult.succeed("Task 1"));
                });
            }
        };
        final FakeAsyncSupplier<String> task2 = new FakeAsyncSupplier<String>() {
            @Override
            public void accept(Handler<AsyncResult<String>> u) {
                rule.vertx().setTimer(100, id -> {
                    incrementRunCount();
                    u.handle(DefaultAsyncResult.succeed("Task 2"));
                });
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.race(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1, task2), result -> {
            context.assertEquals(0, task1.runCount());
            context.assertEquals(1, task2.runCount());
            context.assertNotNull(result);
            context.assertTrue(result.succeeded());
            context.assertEquals(result.result(), "Task 2");
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void raceExecutesTaskInFails(final TestContext context) {
        final FakeAsyncSupplier<String> task1 = new FakeAsyncSupplier<String>() {
            @Override
            public void accept(Handler<AsyncResult<String>> u) {
                rule.vertx().setTimer(200, id -> {
                    incrementRunCount();
                    u.handle(DefaultAsyncResult.succeed("Task 1"));
                });
            }
        };
        final FakeAsyncSupplier<String> task2 = new FakeAsyncSupplier<String>() {
            @Override
            public void accept(Handler<AsyncResult<String>> u) {
                rule.vertx().setTimer(100, id -> {
                    incrementRunCount();
                    u.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
                });
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.race(Arrays.<Consumer<Handler<AsyncResult<String>>>>asList(task1, task2), result -> {
            context.assertEquals(0, task1.runCount());
            context.assertEquals(1, task2.runCount());
            context.assertFalse(result.succeeded());
            context.assertNotNull(result);
            context.assertNull(result.result());
            context.assertTrue(result.cause() instanceof IllegalArgumentException);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void raceExecutesTaskWithExceptionRaised(final TestContext context) {
        final FakeAsyncSupplier<String> task1 = new FakeAsyncSupplier<String>() {
            @Override
            public void accept(Handler<AsyncResult<String>> u) {
                rule.vertx().setTimer(200, id -> {
                    incrementRunCount();
                    u.handle(DefaultAsyncResult.succeed("Task 1"));
                });
            }
        };
        final FakeAsyncSupplier<String> task2 = new FakeAsyncSupplier<String>() {
            @Override
            public void accept(Handler<AsyncResult<String>> u) {
                incrementRunCount();
                throw new IllegalAccessError();
            }
        };
        final AtomicInteger handlerCallCount = new AtomicInteger(0);
        final Async async = context.async();

        AsyncFlows.race(Arrays.asList(task1, task2), result -> {
            context.assertEquals(0, task1.runCount());
            context.assertEquals(1, task2.runCount());
            context.assertFalse(result.succeeded());
            context.assertNotNull(result);
            context.assertNull(result.result());
            context.assertTrue(result.cause() instanceof IllegalAccessError);
            context.assertEquals(1, handlerCallCount.incrementAndGet());
            async.complete();
        });
    }

    @Test
    public void createQueue(final TestContext context) {
        context.assertNotNull(AsyncFlows.<Integer>queue((t, u) -> {
            rule.vertx().setTimer(t, event -> {
                u.handle(DefaultAsyncResult.succeed());
            });
        }));
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void eachWithNoFunctions(final TestContext context) {
        final Async async = context.async();
        AsyncFlows.each(Arrays.<BiConsumer<String, Handler<AsyncResult<Void>>>>asList(), "TEST", result -> {
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void eachWithSingleFunction(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger(0);
        final Async async = context.async();
        AsyncFlows.each(Arrays.asList((t, u) -> {
            context.assertEquals("TEST", t);
            counter.incrementAndGet();
            u.handle(DefaultAsyncResult.succeed());
        }), "TEST", result -> {
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(1, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void eachWithtwoFunctions(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger(0);
        final Async async = context.async();
        final BiConsumer<String, Handler<AsyncResult<Void>>> function = (t, u) -> {
            context.assertEquals("TEST2", t);
            counter.incrementAndGet();
            u.handle(DefaultAsyncResult.succeed());
        };

        AsyncFlows.each(Arrays.asList(function, function), "TEST2", result -> {
            context.assertTrue(result.succeeded());
            context.assertNull(result.result());
            context.assertEquals(2, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void eachWithFailingFunction(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger(0);
        final Async async = context.async();
        AsyncFlows.each(Arrays.asList((t, u) -> {
            context.assertEquals("TEST3", t);
            counter.incrementAndGet();
            u.handle(DefaultAsyncResult.succeed());
        }, (t, u) -> {
            context.assertEquals("TEST3", t);
            counter.incrementAndGet();
            u.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
        }), "TEST3", result -> {
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertTrue(result.cause() instanceof IllegalArgumentException);
            context.assertEquals(2, counter.get());
            async.complete();
        });
    }

    @Test(timeout = AsyncFlowsTest.TIMEOUT_LIMIT)
    @Repeat(AsyncFlowsTest.REPEAT_LIMIT)
    public void eachWithRaisExceptionFunction(final TestContext context) {
        final AtomicInteger counter = new AtomicInteger(0);
        final Async async = context.async();
        AsyncFlows.each(Arrays.asList((t, u) -> {
            context.assertEquals("TEST3", t);
            counter.incrementAndGet();
            u.handle(DefaultAsyncResult.succeed());
        }, (t, u) -> {
            counter.incrementAndGet();
            throw new ClassCastException();
        }), "TEST3", result -> {
            context.assertFalse(result.succeeded());
            context.assertNull(result.result());
            context.assertTrue(result.cause() instanceof ClassCastException);
            context.assertEquals(2, counter.get());
            async.complete();
        });
    }

}
