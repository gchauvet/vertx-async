package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.ObjectWrapper;
import org.simondean.vertx.async.unit.fakes.FakeFailingAsyncSupplier;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulAsyncSupplier;
import org.simondean.vertx.async.unit.fakes.FakeVertx;

import static org.assertj.core.api.Assertions.assertThat;

public class ForeverTest {
  @Test
  public void itExecutesTheTaskUntilItFails() {
    FakeFailingAsyncSupplier<Void> task1 = new FakeFailingAsyncSupplier<>(2, null, new Throwable("Failed"));

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.forever()
      .task(task1)
      .run(new FakeVertx(), result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertThat(task1.runCount()).isEqualTo(3);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        Object resultValue = result.result();
        assertThat(resultValue).isNull();
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
  }
}
