package io.zatarox.vertx.async;

import java.util.Arrays;
import org.junit.Test;
import io.zatarox.vertx.async.examples.EachExample;

import java.util.List;

import static org.junit.Assert.*;

public class EachExampleTest {

    @Test
    public void itHandlesSuccess() {
        EachExample example = new EachExample(true);

        example.eachExample(result -> {
            assertNotNull(result);
            assertTrue(result.succeeded());

            assertNull(result.result());
            List<String> items = example.items();
            assertNotNull(items);
            assertTrue(items.containsAll(Arrays.asList("one", "two", "three")));
        });
    }

    @Test
    public void itHandlesFailure() {
        EachExample example = new EachExample(false);

        example.eachExample(result -> {
            assertNotNull(result);
            assertFalse(result.succeeded());

            assertNull(result.result());
            List<String> items = example.items();
            assertNotNull(items);
            assertTrue(items.isEmpty());
        });
    }
}