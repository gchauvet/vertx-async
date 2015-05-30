package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.Series;
import org.simondean.vertx.async.unit.fakes.FakeFailingTask;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulTask;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SeriesTest {
  @Test
  public void itStillExecutesWhenThereAreNoTasks() {
    Series<Object> series = new Series<>();

    series.run(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isTrue();
      List<Object> resultList = result.result();
      assertThat(resultList).isNotNull();
      assertThat(resultList).isEmpty();
    });
  }

  @Test
  public void itExecutesOneTask() {
    Series<Object> series = new Series<>();

    FakeSuccessfulTask<Object> task1 = new FakeSuccessfulTask<>("Task 1");

    series
      .task(task1)
      .run(result -> {
        assertThat(task1.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        List<Object> resultList = result.result();
        assertThat(resultList).isNotNull();
        assertThat(resultList).containsExactly(task1.result());
      });
  }

  @Test
  public void itExecutesTwoTasks() {
    Series<Object> series = new Series<>();

    FakeSuccessfulTask<Object> task1 = new FakeSuccessfulTask<>("Task 1");
    FakeSuccessfulTask<Object> task2 = new FakeSuccessfulTask<>("Task 2");

    series
      .task(task1)
      .task(task2)
      .run(result -> {
        assertThat(task1.runCount()).isEqualTo(1);
        assertThat(task2.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        List<Object> resultList = result.result();
        assertThat(resultList).isNotNull();
        assertThat(resultList).containsExactly(task1.result(), task2.result());
      });
  }

  @Test
  public void itFailsWhenATaskFails() {
    Series<Object> series = new Series<>();

    FakeFailingTask<Object> task1 = new FakeFailingTask<>(new Throwable("Failed"));

    series
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
    Series<Object> series = new Series<>();

    FakeFailingTask<Object> task1 = new FakeFailingTask<>(new Throwable("Failed"));
    series.task(task1);
    FakeSuccessfulTask<Object> task2 = new FakeSuccessfulTask<>("Task 2");
    series.task(task2);

    series.run(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isFalse();
      assertThat(result.cause()).isEqualTo(task1.cause());
      assertThat(result.result()).isNull();
      assertThat(task1.runCount()).isEqualTo(1);
      assertThat(task2.runCount()).isEqualTo(0);
    });
  }
}
