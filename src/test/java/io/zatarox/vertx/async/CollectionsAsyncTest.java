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

import io.zatarox.vertx.async.fakes.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

public final class CollectionsAsyncTest {

    @Test
    public void itStillExecutesWhenThereAreNoItems() {
        final List<String> items = new ArrayList<>();
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.each(new FakeVertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertNotNull(result);
            assertTrue(result.succeeded());
            assertNull(result.result());
            assertEquals(0, each.runCount());
        });

        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itExecutesForOneItem() {
        final List<String> items = Arrays.asList("One");
        final FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.each(new FakeVertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertNotNull(result);
            assertTrue(result.succeeded());
            assertNull(result.result());

            assertEquals(1, each.runCount());
            assertTrue(each.consumedValues().containsAll(Arrays.asList("One")));
        });

        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itExecutesForTwoItems() {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.each(new FakeVertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertNotNull(result);
            assertTrue(result.succeeded());
            assertNull(result.result());

            assertEquals(2, each.runCount());
            assertTrue(each.consumedValues().containsAll(Arrays.asList("One", "Two")));
        });

        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itFailsWhenAnItemFails() {
        final List<String> items = Arrays.asList("One");
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.each(new FakeVertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertNotNull(result);
            assertFalse(result.succeeded());
            assertEquals(each.cause(), result.cause());
            assertNull(result.result());

            assertEquals(1, each.runCount());
            assertTrue(each.consumedValues().containsAll(items));
        });

        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itFailsNoMoreThanOnce() {
        final List<String> items = Arrays.asList("One", "Two");
        final FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> resultCount = new ObjectWrapper<>(0);
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.each(new FakeVertx(), items, each, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertNotNull(result);
            assertFalse(result.succeeded());
            assertEquals(each.cause(), result.cause());
            assertNull(result.result());

            resultCount.setObject(resultCount.getObject() + 1);

            assertEquals(1, resultCount.getObject().intValue());
        });

        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itStillExecutesWhenThereAreNoTasks() {
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.series(new FakeVertx(), Arrays.asList(), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertNotNull(result);
            assertTrue(result.succeeded());
            List<Object> resultList = result.result();
            assertNotNull(resultList);
            assertTrue(resultList.isEmpty());
        });
        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itExecutesOneTask() {
        final FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.series(new FakeVertx(), Arrays.asList(task1), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertEquals(1, task1.runCount());
            assertNotNull(result);
            assertTrue(result.succeeded());
            List<Object> resultList = result.result();
            assertNotNull(resultList);
            assertTrue(resultList.containsAll(Arrays.asList(task1.result())));
        });
        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itExecutesTwoTasks() {
        final FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.series(new FakeVertx(), Arrays.asList(task1, task2), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertEquals(1, task1.runCount());
            assertEquals(1, task2.runCount());
            assertNotNull(result);
            assertTrue(result.succeeded());
            List<Object> resultList = result.result();
            assertNotNull(resultList);
            assertTrue(resultList.containsAll(Arrays.asList(task1.result(), task2.result())));
        });
        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itFailsWhenATaskFails() {
        final FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.series(new FakeVertx(), Arrays.asList(task1), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertEquals(1, task1.runCount());
            assertNotNull(result);
            assertFalse(result.succeeded());
            assertEquals(task1.cause(), result.cause());
            assertNull(result.result());
        });
        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itExecutesNoMoreTasksWhenATaskFails() {
        final FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        final FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        CollectionsAsync.series(new FakeVertx(), Arrays.asList(task1, task2), result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertNotNull(result);
            assertFalse(result.succeeded());
            assertEquals(task1.cause(), result.cause());
            assertNull(result.result());
            assertEquals(1, (int) task1.runCount());
            assertEquals(0, (int) task2.runCount());
        });
        assertEquals(1, (int) handlerCallCount.getObject());
    }
}
