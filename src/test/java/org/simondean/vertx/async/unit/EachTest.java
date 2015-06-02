package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.ObjectWrapper;
import org.simondean.vertx.async.unit.fakes.*;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class EachTest {
  @Test
  public void itStillExecutesWhenThereAreNoItems() {
    ArrayList<String> items = new ArrayList<>();

    FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));

    ObjectWrapper<Boolean> handlerCalled = new ObjectWrapper<>(false);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        handlerCalled.setObject(true);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        assertThat(result.result()).isNull();

        assertThat(each.runCount()).isEqualTo(0);
      });

    assertThat(handlerCalled.getObject()).isTrue();
  }

  @Test
  public void itExecutesForOneItem() {
    ArrayList<String> items = new ArrayList<>();
    items.add("One");

    FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);

    ObjectWrapper<Boolean> handlerCalled = new ObjectWrapper<>(false);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        handlerCalled.setObject(true);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        assertThat(result.result()).isNull();

        assertThat(each.runCount()).isEqualTo(1);
        assertThat(each.consumedValues()).containsExactly("One");
      });

    assertThat(handlerCalled.getObject()).isTrue();
  }

  @Test
  public void itExecutesForTwoItems() {
    ArrayList<String> items = new ArrayList<>();
    items.add("One");
    items.add("Two");

    FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);

    ObjectWrapper<Boolean> handlerCalled = new ObjectWrapper<>(false);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        handlerCalled.setObject(true);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        assertThat(result.result()).isNull();

        assertThat(each.runCount()).isEqualTo(2);
        assertThat(each.consumedValues()).containsExactly("One", "Two");
      });

    assertThat(handlerCalled.getObject()).isTrue();
  }

  @Test
  public void itFailsWhenAnItemFails() {
    ArrayList<String> items = new ArrayList<>();
    items.add("One");

    FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));

    ObjectWrapper<Boolean> handlerCalled = new ObjectWrapper<>(false);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        handlerCalled.setObject(true);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(each.cause());
        assertThat(result.result()).isNull();

        assertThat(each.runCount()).isEqualTo(1);
        assertThat(each.consumedValues()).containsExactlyElementsOf(items);
      });

    assertThat(handlerCalled.getObject()).isTrue();
  }

  @Test
  public void itFailsNoMoreThanOnce() {
    ArrayList<String> items = new ArrayList<>();
    items.add("One");
    items.add("Two");

    FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
    ObjectWrapper<Integer> resultCount = new ObjectWrapper<>(0);

    ObjectWrapper<Boolean> handlerCalled = new ObjectWrapper<>(false);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        handlerCalled.setObject(true);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(each.cause());
        assertThat(result.result()).isNull();

        resultCount.setObject(resultCount.getObject().intValue() + 1);

        assertThat(resultCount.getObject().intValue()).isEqualTo(1);
      });

    assertThat(handlerCalled.getObject()).isTrue();
  }
}
