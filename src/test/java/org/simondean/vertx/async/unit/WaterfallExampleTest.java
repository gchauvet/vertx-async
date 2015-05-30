package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.unit.examples.WaterfallExample;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WaterfallExampleTest {
  @Test
  public void itHandlesSuccess() {
    WaterfallExample example = new WaterfallExample(true);

    example.waterfallExample(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isTrue();

      Integer resultsFromHandler = result.result();
      assertThat(resultsFromHandler).isNotNull();
      assertThat(resultsFromHandler).isEqualTo(42);
      Integer resultsFromExample = example.result();
      assertThat(resultsFromExample).isNotNull();
      assertThat(resultsFromExample).isEqualTo(42);
    });
  }

  @Test
  public void itHandlesFailure() {
    WaterfallExample example = new WaterfallExample(false);

    example.waterfallExample(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isFalse();

      Integer resultsFromHandler = result.result();
      assertThat(resultsFromHandler).isNull();
      Integer resultsFromExample = example.result();
      assertThat(resultsFromExample).isNull();
    });
  }
}
