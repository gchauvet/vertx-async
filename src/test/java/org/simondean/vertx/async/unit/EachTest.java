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

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        assertThat(result.result()).isNull();

        assertThat(each.runCount()).isEqualTo(0);
      });
  }

  @Test
  public void itExecutesForOneItem() {
    ArrayList<String> items = new ArrayList<>();
    items.add("One");

    FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        assertThat(result.result()).isNull();

        assertThat(each.runCount()).isEqualTo(1);
        assertThat(each.consumedValues()).containsExactly("One");
      });
  }

  @Test
  public void itExecutesForTwoItems() {
    ArrayList<String> items = new ArrayList<>();
    items.add("One");
    items.add("Two");

    FakeSuccessfulAsyncFunction<String, Void> each = new FakeSuccessfulAsyncFunction<>(null);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        assertThat(result.result()).isNull();

        assertThat(each.runCount()).isEqualTo(2);
        assertThat(each.consumedValues()).containsExactly("One", "Two");
      });
  }

  @Test
  public void itFailsWhenAnItemFails() {
    ArrayList<String> items = new ArrayList<>();
    items.add("One");

    FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(each.cause());
        assertThat(result.result()).isNull();

        assertThat(each.runCount()).isEqualTo(1);
        assertThat(each.consumedValues()).containsExactlyElementsOf(items);
      });
  }

  @Test
  public void itFailsNoMoreThanOnce() {
    ArrayList<String> items = new ArrayList<>();
    items.add("One");
    items.add("Two");

    FakeFailingAsyncFunction<String, Void> each = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
    ObjectWrapper<Integer> resultCount = new ObjectWrapper<>(0);

    Async.iterable(items)
      .each(each)
      .run(new FakeVertx(), result -> {
        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(each.cause());
        assertThat(result.result()).isNull();

        resultCount.setObject(resultCount.getObject().intValue() + 1);

        assertThat(resultCount.getObject().intValue()).isEqualTo(1);
      });
  }
}
