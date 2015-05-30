package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.Series;
import org.simondean.vertx.async.EmptyWaterfall;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncTest {
  @Test
  public void itCreatesANewSeries() {
    Series<Object> series = Async.series();
    assertThat(series).isNotNull();
  }

  @Test
  public void itCreatesANewWaterfall() {
    EmptyWaterfall waterfallBuilder = Async.waterfall();
    assertThat(waterfallBuilder).isNotNull();
  }
}