package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.*;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncTest {
  @Test
  public void itCreatesANewSeries() {
    Series<Object> series = Async.series();
    assertThat(series).isNotNull();
  }

  @Test
  public void itCreatesANewWaterfall() {
    WaterfallBuilder waterfallBuilder = Async.waterfall();
    assertThat(waterfallBuilder).isNotNull();
  }

  @Test
  public void itCreatesANewIterable() {
    List<String> list = Arrays.asList("One");
    IterableBuilder<String> iterableBuilder = Async.iterable(list);
    assertThat(iterableBuilder).isNotNull();
  }

  @Test
  public void itCreatesANewRetry() {
    RetryBuilder retryBuilder = Async.retry();
    assertThat(retryBuilder).isNotNull();
  }

  @Test
  public void itCreatesANewForever() {
    ForeverBuilder foreverBuilder = Async.forever();
    assertThat(foreverBuilder).isNotNull();
  }
}
