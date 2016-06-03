package org.simondean.vertx.async.unit;

import java.util.Arrays;
import org.junit.Test;
import org.simondean.vertx.async.unit.examples.SeriesExample;

import java.util.List;

import static org.junit.Assert.*;

public class SeriesExampleTest {

    @Test
    public void itHandlesSuccess() {
        SeriesExample example = new SeriesExample(true);

        example.seriesExample(result -> {
            assertNotNull(result);
            assertTrue(result.succeeded());

            List<String> resultsFromHandler = result.result();
            assertNotNull(resultsFromHandler);
            assertTrue(resultsFromHandler.containsAll(Arrays.asList("Result", "Async result")));
            List<String> resultsFromExample = example.results();
            assertNotNull(resultsFromExample);
            assertTrue(resultsFromExample.containsAll(Arrays.asList("Result", "Async result")));
        });
    }

    @Test
    public void itHandlesFailure() {
        SeriesExample example = new SeriesExample(false);

        example.seriesExample(result -> {
            assertNotNull(result);
            assertFalse(result.succeeded());

            List<String> resultsFromHandler = result.result();
            assertNull(resultsFromHandler);
            List<String> resultsFromExample = example.results();
            assertNull(resultsFromExample);
        });
    }
}
