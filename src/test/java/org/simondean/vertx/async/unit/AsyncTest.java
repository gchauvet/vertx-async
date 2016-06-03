package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AsyncTest {

    @Test
    public void itCreatesANewSeries() {
        Series<Object> series = Async.series();
        assertNotNull(series);
    }

    @Test
    public void itCreatesANewWaterfall() {
        WaterfallBuilder waterfallBuilder = Async.waterfall();
        assertNotNull(waterfallBuilder);
    }

    @Test
    public void itCreatesANewIterable() {
        List<String> list = Arrays.asList("One");
        IterableBuilder<String> iterableBuilder = Async.iterable(list);
        assertNotNull(iterableBuilder);
    }

    @Test
    public void itCreatesANewRetry() {
        RetryBuilder retryBuilder = Async.retry();
        assertNotNull(retryBuilder);
    }

    @Test
    public void itCreatesANewForever() {
        ForeverBuilder foreverBuilder = Async.forever();
        assertNotNull(foreverBuilder);
    }
}
