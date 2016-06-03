package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.ObjectWrapper;
import org.simondean.vertx.async.unit.fakes.*;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class EachTest {
  @Test
  public void itStillExecutesWhenThereAreNoItems() {
    ArrayList<String> items = new ArrayList<>();

    FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
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
    ArrayList<String> items = new ArrayList<>();
    items.add("One");

    FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
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
    ArrayList<String> items = new ArrayList<>();
    items.add("One");
    items.add("Two");

    FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
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
    ArrayList<String> items = new ArrayList<>();
    items.add("One");

    FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
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
    ArrayList<String> items = new ArrayList<>();
    items.add("One");
    items.add("Two");

    FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
    ObjectWrapper<Integer> resultCount = new ObjectWrapper<>(0);

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
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
