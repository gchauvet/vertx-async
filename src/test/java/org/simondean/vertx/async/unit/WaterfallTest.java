package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.unit.fakes.FakeFailingAsyncFunction;
import org.simondean.vertx.async.unit.fakes.FakeFailingAsyncSupplier;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulAsyncFunction;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulAsyncSupplier;

import static org.assertj.core.api.Assertions.assertThat;

public class WaterfallTest {
  @Test
  public void itExecutesOneTask() {
    FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");

    Async.waterfall()
      .task(task1)
      .run(result -> {
        assertThat(task1.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        String resultValue = result.result();
        assertThat(resultValue).isNotNull();
        assertThat(resultValue).isEqualTo(task1.result());
      });
  }

  @Test
  public void itExecutesTwoTasks() {
    FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
    FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);

    Async.waterfall()
      .task(task1)
      .task(task2)
      .run(result -> {
        assertThat(task1.runCount()).isEqualTo(1);
        assertThat(task2.consumedValue()).isEqualTo(task1.result());
        assertThat(task2.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        Integer resultValue = result.result();
        assertThat(resultValue).isNotNull();
        assertThat(resultValue).isEqualTo(task2.result());
      });
  }

  @Test
  public void itFailsWhenATaskFails() {
    FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));

    Async.waterfall()
      .task(task1)
      .run(result -> {
        assertThat(task1.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(task1.cause());
        assertThat(result.result()).isNull();
      });
  }

  @Test
  public void itExecutesNoMoreTasksWhenATaskFails() {
    FakeFailingAsyncSupplier<String> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
    FakeSuccessfulAsyncFunction<String, Integer> task2 = new FakeSuccessfulAsyncFunction<>(2);

    Async.waterfall()
      .task(task1)
      .task(task2)
      .run(result -> {
        assertThat(task1.runCount()).isEqualTo(1);
        assertThat(task2.runCount()).isEqualTo(0);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(task1.cause());
        assertThat(result.result()).isNull();
      });
  }

  @Test
  public void itFailsWhenAConsumerTaskFails() {
    FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
    FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));

    Async.waterfall()
      .task(task1)
      .task(task2)
      .run(result -> {
        assertThat(task1.runCount()).isEqualTo(1);
        assertThat(task2.consumedValue()).isEqualTo(task1.result());
        assertThat(task2.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(task2.cause());
        assertThat(result.result()).isNull();
      });
  }

  @Test
  public void itExecutesNoMoreTasksWhenAConsumerTaskFails() {
    FakeSuccessfulAsyncSupplier<String> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
    FakeFailingAsyncFunction<String, Integer> task2 = new FakeFailingAsyncFunction<>(new Throwable("Failed"));
    FakeSuccessfulAsyncFunction<Integer, String> task3 = new FakeSuccessfulAsyncFunction<>("Task 3");

    Async.waterfall()
      .task(task1)
      .task(task2)
      .task(task3)
      .run(result -> {
        assertThat(task1.runCount()).isEqualTo(1);
        assertThat(task2.consumedValue()).isEqualTo(task1.result());
        assertThat(task2.runCount()).isEqualTo(1);
        assertThat(task3.runCount()).isEqualTo(0);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(task2.cause());
        assertThat(result.result()).isNull();
      });
  }
}
