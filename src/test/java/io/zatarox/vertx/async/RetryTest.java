package io.zatarox.vertx.async;

import org.junit.Test;
import io.zatarox.vertx.async.fakes.FakeFailingAsyncSupplier;
import io.zatarox.vertx.async.fakes.FakeSuccessfulAsyncSupplier;

import static org.junit.Assert.*;

public class RetryTest {

    @Test
    public void itExecutesTheTask() {
        FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        Async.retry()
                .task(task1)
                .times(1)
                .run(result -> {
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
        FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>(1, new Throwable("Failed"), "Task 1");

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        Async.retry()
                .task(task1)
                .times(1)
                .run(result -> {
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
        FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>(2, new Throwable("Failed"), "Task 1");

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        Async.retry()
                .task(task1)
                .times(2)
                .run(result -> {
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

        Async.retry()
                .task(task1)
                .times(1)
                .run(result -> {
                    handlerCallCount.setObject(handlerCallCount.getObject() + 1);

                    assertEquals(2, task1.runCount());

                    assertNotNull(result);
                    assertFalse(result.succeeded());
                    assertEquals(task1.cause(), result.cause());
                    assertNull(result.result());
                });

        assertEquals(1, (int) handlerCallCount.getObject());
    }
}
