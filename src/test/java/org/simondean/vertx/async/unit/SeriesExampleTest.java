package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.unit.examples.SeriesExample;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SeriesExampleTest {
  @Test
  public void itHandlesSuccess() {
    SeriesExample example = new SeriesExample(true);

    example.seriesExample(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isTrue();

      List<String> resultsFromHandler = result.result();
      assertThat(resultsFromHandler).isNotNull();
      assertThat(resultsFromHandler).containsExactly("Result", "Async result");
      List<String> resultsFromExample = example.results();
      assertThat(resultsFromExample).isNotNull();
      assertThat(resultsFromExample).containsExactly("Result", "Async result");
    });
  }

  @Test
  public void itHandlesFailure() {
    SeriesExample example = new SeriesExample(false);

    example.seriesExample(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isFalse();

      List<String> resultsFromHandler = result.result();
      assertThat(resultsFromHandler).isNull();
      List<String> resultsFromExample = example.results();
      assertThat(resultsFromExample).isNull();
    });
  }
}
