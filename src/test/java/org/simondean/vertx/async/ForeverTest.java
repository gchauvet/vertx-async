package org.simondean.vertx.async;

import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.ObjectWrapper;
import org.simondean.vertx.async.fakes.FakeFailingAsyncSupplier;
import org.simondean.vertx.async.fakes.FakeVertx;

import static org.junit.Assert.*;

public class ForeverTest {

    @Test
    public void itExecutesTheTaskUntilItFails() {
        FakeFailingAsyncSupplier<Void> task1 = new FakeFailingAsyncSupplier<>(2, null, new Throwable("Failed"));

        ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

        Async.forever()
                .task(task1)
                .run(new FakeVertx(), result -> {
                    handlerCallCount.setObject(handlerCallCount.getObject() + 1);

                    assertEquals(3, task1.runCount());

                    assertNotNull(result);
                    assertFalse(result.succeeded());
                    Object resultValue = result.result();
                    assertNull(resultValue);
                });

        assertEquals(1, (int) handlerCallCount.getObject());
    }
}
