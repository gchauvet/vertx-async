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
import io.zatarox.vertx.async.fakes.FakeFailingAsyncFunction;
import io.zatarox.vertx.async.fakes.FakeSuccessfulAsyncFunction;
import io.zatarox.vertx.async.fakes.FakeVertx;
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
}