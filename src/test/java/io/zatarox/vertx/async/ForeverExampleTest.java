package io.zatarox.vertx.async;

import org.junit.Test;
import io.zatarox.vertx.async.examples.ForeverExample;

import static org.junit.Assert.*;

public class ForeverExampleTest {

    @Test
    public void itHandlesFailure() {
        ForeverExample example = new ForeverExample();

        example.foreverExample(result -> {
            assertNotNull(result);
            assertFalse(result.succeeded());

            String resultFromHandler = result.result();
            assertNull(resultFromHandler);
        });
    }
}
