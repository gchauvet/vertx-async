package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.unit.examples.RetryExample;

import static org.assertj.core.api.Assertions.assertThat;

public class RetryExampleTest {
  @Test
  public void itHandlesSuccess() {
    RetryExample example = new RetryExample(true);

    example.retryExample(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isTrue();

      String resultFromHandler = result.result();
      assertThat(resultFromHandler).isNotNull();
      assertThat(resultFromHandler).isEqualTo("Async result");
      String resultFromExample = example.result();
      assertThat(resultFromExample).isNotNull();
      assertThat(resultFromExample).isEqualTo("Async result");
    });
  }

  @Test
  public void itHandlesFailure() {
    RetryExample example = new RetryExample(false);

    example.retryExample(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isFalse();

      String resultFromHandler = result.result();
      assertThat(resultFromHandler).isNull();
      String resultFromExample = example.result();
      assertThat(resultFromExample).isNull();
    });
  }
}
