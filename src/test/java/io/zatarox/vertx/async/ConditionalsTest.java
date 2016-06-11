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

import io.zatarox.vertx.async.fakes.FakeFailingAsyncSupplier;
import io.zatarox.vertx.async.fakes.FakeSuccessfulAsyncSupplier;
import io.zatarox.vertx.async.fakes.FakeVertx;
import static org.junit.Assert.*;
import org.junit.Test;

public final class ConditionalsTest {

    @Test
    public void itExecutesTheTask() {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        ConditionalsAsync.retry(new FakeVertx(), task1, 1, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertEquals(1, task1.runCount());
            assertNotNull(result);
            assertTrue(result.succeeded());
            String resultValue = result.result();
            assertNotNull(resultValue);
            assertEquals(task1.result(), resultValue);
        });
        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itExecutesTheTaskAgainAfterAFailure() {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>(1, new Throwable("Failed"), "Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        ConditionalsAsync.retry(new FakeVertx(), task1, 1, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertEquals(2, task1.runCount());
            assertNotNull(result);
            assertTrue(result.succeeded());
            String resultValue = result.result();
            assertNotNull(resultValue);
            assertEquals(task1.result(), resultValue);
        });

        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itExecutesTheTaskAgainAfterASecondFailure() {
        final FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>(2, new Throwable("Failed"), "Task 1");
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        ConditionalsAsync.retry(new FakeVertx(), task1, 2, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertEquals(3, task1.runCount());
            assertNotNull(result);
            assertTrue(result.succeeded());
            String resultValue = result.result();
            assertNotNull(resultValue);
            assertEquals(task1.result(), resultValue);
        });
        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itFailsAfterTheRetryTimes() {
        FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        ConditionalsAsync.retry(new FakeVertx(), task1, 1, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertEquals(2, task1.runCount());
            assertNotNull(result);
            assertFalse(result.succeeded());
            assertEquals(task1.cause(), result.cause());
            assertNull(result.result());
        });

        assertEquals(1, (int) handlerCallCount.getObject());
    }

    @Test
    public void itExecutesTheTaskUntilItFails() {
        final FakeFailingAsyncSupplier<Void> task1 = new FakeFailingAsyncSupplier<>(2, null, new Throwable("Failed"));
        final ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        ConditionalsAsync.forever(new FakeVertx(), task1, result -> {
            handlerCallCount.setObject(handlerCallCount.getObject() + 1);

            assertEquals(3, task1.runCount());
            assertNotNull(result);
            assertFalse(result.succeeded());
            Object resultValue = result.result();
            assertNull(resultValue);
        });
        assertEquals(1, (int) handlerCallCount.getObject());
    }
}
