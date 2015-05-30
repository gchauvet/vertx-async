package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.WaterfallBuilder;
import org.simondean.vertx.async.internal.WaterfallBuilderImpl;
import org.simondean.vertx.async.unit.fakes.FakeFailingConsumerTask;
import org.simondean.vertx.async.unit.fakes.FakeFailingTask;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulConsumerTask;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulTask;

import static org.assertj.core.api.Assertions.assertThat;

public class WaterfallTest {
  @Test
  public void itExecutesOneTask() {
    WaterfallBuilder waterfallBuilder = new WaterfallBuilderImpl();

    FakeSuccessfulTask<String> task1 = new FakeSuccessfulTask<>("Task 1");
    
    waterfallBuilder
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
    WaterfallBuilder waterfallBuilder = new WaterfallBuilderImpl();

    FakeSuccessfulTask<String> task1 = new FakeSuccessfulTask<>("Task 1");
    FakeSuccessfulConsumerTask<String, Integer> task2 = new FakeSuccessfulConsumerTask<>(2);

    waterfallBuilder
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
    WaterfallBuilder waterfallBuilder = new WaterfallBuilderImpl();

    FakeFailingTask<String> task1 = new FakeFailingTask<>(new Throwable("Failed"));

    waterfallBuilder
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
    WaterfallBuilder waterfallBuilder = new WaterfallBuilderImpl();

    FakeFailingTask<String> task1 = new FakeFailingTask<>(new Throwable("Failed"));
    FakeSuccessfulConsumerTask<String, Integer> task2 = new FakeSuccessfulConsumerTask<>(2);

    waterfallBuilder
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
    WaterfallBuilder waterfallBuilder = new WaterfallBuilderImpl();

    FakeSuccessfulTask<String> task1 = new FakeSuccessfulTask<>("Task 1");
    FakeFailingConsumerTask<String, Integer> task2 = new FakeFailingConsumerTask<>(new Throwable("Failed"));

    waterfallBuilder
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
    WaterfallBuilder waterfallBuilder = new WaterfallBuilderImpl();

    FakeSuccessfulTask<String> task1 = new FakeSuccessfulTask<>("Task 1");
    FakeFailingConsumerTask<String, Integer> task2 = new FakeFailingConsumerTask<>(new Throwable("Failed"));
    FakeSuccessfulConsumerTask<Integer, String> task3 = new FakeSuccessfulConsumerTask<>("Task 3");

    waterfallBuilder
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
