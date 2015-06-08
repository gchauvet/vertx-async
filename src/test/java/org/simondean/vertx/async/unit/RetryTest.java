package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.ObjectWrapper;
import org.simondean.vertx.async.unit.fakes.FakeFailingAsyncSupplier;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulAsyncSupplier;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(task1.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        String resultValue = result.result();
        assertThat(resultValue).isNotNull();
        assertThat(resultValue).isEqualTo(task1.result());
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
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

        assertThat(task1.runCount()).isEqualTo(2);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        String resultValue = result.result();
        assertThat(resultValue).isNotNull();
        assertThat(resultValue).isEqualTo(task1.result());
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
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

        assertThat(task1.runCount()).isEqualTo(3);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        String resultValue = result.result();
        assertThat(resultValue).isNotNull();
        assertThat(resultValue).isEqualTo(task1.result());
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
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

        assertThat(task1.runCount()).isEqualTo(2);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(task1.cause());
        assertThat(result.result()).isNull();
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
  }
}
