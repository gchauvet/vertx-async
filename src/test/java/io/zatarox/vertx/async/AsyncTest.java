package io.zatarox.vertx.async;

import org.junit.Test;

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
