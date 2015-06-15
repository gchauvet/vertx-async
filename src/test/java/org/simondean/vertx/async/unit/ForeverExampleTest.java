package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.unit.examples.ForeverExample;

import static org.assertj.core.api.Assertions.assertThat;

public class ForeverExampleTest {
  @Test
  public void itHandlesFailure() {
    ForeverExample example = new ForeverExample();

    example.foreverExample(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isFalse();

      String resultFromHandler = result.result();
      assertThat(resultFromHandler).isNull();
    });
  }
}
