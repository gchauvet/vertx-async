package org.simondean.vertx.async;

import org.junit.Test;
import org.simondean.vertx.async.examples.RetryExample;

import static org.junit.Assert.*;

public class RetryExampleTest {

    @Test
    public void itHandlesSuccess() {
        RetryExample example = new RetryExample(true);

        example.retryExample(result -> {
            assertNotNull(result);
            assertTrue(result.succeeded());

            String resultFromHandler = result.result();
            assertNotNull(resultFromHandler);
            assertEquals("Async result", resultFromHandler);
            String resultFromExample = example.result();
            assertNotNull(resultFromExample);
            assertEquals("Async result", resultFromExample);
        });
    }

    @Test
    public void itHandlesFailure() {
        RetryExample example = new RetryExample(false);

        example.retryExample(result -> {
            assertNotNull(result);
            assertFalse(result.succeeded());

            String resultFromHandler = result.result();
            assertNull(resultFromHandler);
            String resultFromExample = example.result();
            assertNull(resultFromExample);
        });
    }
}
