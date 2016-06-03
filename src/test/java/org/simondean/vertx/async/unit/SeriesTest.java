package org.simondean.vertx.async.unit;

import java.util.Arrays;
import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.ObjectWrapper;
import org.simondean.vertx.async.unit.fakes.FakeFailingAsyncSupplier;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulAsyncSupplier;

import java.util.List;

import static org.junit.Assert.*;

public class SeriesTest {

    @Test
    public void itStillExecutesWhenThereAreNoTasks() {
        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        Async.series()
                .run(result -> {
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
        FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        Async.series()
                .task(task1)
                .run(result -> {
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
        FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
        FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        Async.series()
                .task(task1)
                .task(task2)
                .run(result -> {
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
        FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        Async.series()
                .task(task1)
                .run(result -> {
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
        FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
        FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        Async.series()
                .task(task1)
                .task(task2)
                .run(result -> {
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
