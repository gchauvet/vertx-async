package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.unit.examples.EachExample;
import org.simondean.vertx.async.unit.examples.SeriesExample;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EachExampleTest {
  @Test
  public void itHandlesSuccess() {
    EachExample example = new EachExample(true);

    example.eachExample(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isTrue();

      assertThat(result.result()).isNull();
      List<String> items = example.items();
      assertThat(items).isNotNull();
      assertThat(items).containsExactly("one", "two", "three");
    });
  }

  @Test
  public void itHandlesFailure() {
    EachExample example = new EachExample(false);

    example.eachExample(result -> {
      assertThat(result).isNotNull();
      assertThat(result.succeeded()).isFalse();

      assertThat(result.result()).isNull();
      List<String> items = example.items();
      assertThat(items).isNotNull();
      assertThat(items).isEmpty();
    });
  }
}
