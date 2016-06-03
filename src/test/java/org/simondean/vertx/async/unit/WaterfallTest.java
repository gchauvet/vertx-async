package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.ObjectWrapper;
import org.simondean.vertx.async.unit.fakes.FakeFailingAsyncFunction;
import org.simondean.vertx.async.unit.fakes.FakeFailingAsyncSupplier;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulAsyncFunction;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulAsyncSupplier;

import static org.junit.Assert.*;

public class WaterfallTest {
  @Test
  public void itExecutesOneTask() {
    FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.waterfall()
      .task(task1)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertEquals(1, task1.runCount());

        assertNotNull(result);
        assertTrue(result.succeeded());
        String resultValue = result.result();
        assertNotNull(resultValue);
        assertEquals(task1.result(), resultValue);
      });
  }

  @Test
  public void itExecutesTwoTasks() {
    FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
    FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.waterfall()
      .task(task1)
      .task(task2)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertEquals(1, task1.runCount());
        assertEquals(task1.result(), task2.consumedValue());
        assertEquals(1, task2.runCount());

        assertNotNull(result);
        assertTrue(result.succeeded());
        Integer resultValue = result.result();
        assertNotNull(resultValue);
        assertEquals(task2.result(), resultValue);
      });

    assertEquals(1, (int) handlerCallCount.getObject());
  }

  @Test
  public void itFailsWhenATaskFails() {
    FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.waterfall()
      .task(task1)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertEquals(1, (int) task1.runCount());

        assertNotNull(result);
        assertFalse(result.succeeded());
        assertEquals(task1.cause(), result.cause());
        assertNull(result.result());
      });

    assertEquals(1, (int) handlerCallCount.getObject());
  }

  @Test
  public void itExecutesNoMoreTasksWhenATaskFails() {
    FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
    FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.waterfall()
      .task(task1)
      .task(task2)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertEquals(1, (int) task1.runCount());
        assertEquals(0, task2.runCount());

        assertNotNull(result);
        assertFalse(result.succeeded());
        assertEquals(task1.cause(), result.cause());
        assertNull(result.result());
      });

    assertEquals(1, (int) handlerCallCount.getObject());
  }

  @Test
  public void itFailsWhenAConsumerTaskFails() {
    FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
    FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.waterfall()
      .task(task1)
      .task(task2)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertEquals(1, (int) task1.runCount());
        assertEquals(task1.result(), task2.consumedValue());
        assertEquals(1, task2.runCount());

        assertNotNull(result);
        assertFalse(result.succeeded());
        assertEquals(task2.cause(), result.cause());
        assertNull(result.result());
      });

    assertEquals(1, (int) handlerCallCount.getObject());
  }

  @Test
  public void itExecutesNoMoreTasksWhenAConsumerTaskFails() {
    FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
    FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
    FakeSuccessfulAsyncFunction<Integer, String> task3 = new FakeSuccessfulAsyncFunction<>("Task 3");

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.waterfall()
      .task(task1)
      .task(task2)
      .task(task3)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertEquals(1, (int) task1.runCount());
        assertEquals(task1.result(), task2.consumedValue());
        assertEquals(1, task2.runCount());
        assertEquals(0, task3.runCount());

        assertNotNull(result);
        assertFalse(result.succeeded());
        assertEquals(task2.cause(), result.cause());
        assertNull(result.result());
      });

    assertEquals(1, (int) handlerCallCount.getObject());
  }
}
