package io.zatarox.vertx.async;

import org.junit.Test;


import static org.junit.Assert.*;

public class AsyncTest {

    @Test
    public void itCreatesANewWaterfall() {
        WaterfallBuilder waterfallBuilder = Async.waterfall();
        assertNotNull(waterfallBuilder);
    }

    @Test
    public void itCreatesANewForever() {
        ForeverBuilder foreverBuilder = Async.forever();
        assertNotNull(foreverBuilder);
    }
}
