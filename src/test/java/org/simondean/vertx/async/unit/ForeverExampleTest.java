package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.unit.examples.ForeverExample;

import static org.junit.Assert.*;

public class ForeverExampleTest {
  @Test
  public void itHandlesFailure() {
    ForeverExample example = new ForeverExample();

    example.foreverExample(result -> {
      assertNotNull(result);
      assertFalse(result.succeeded());

      String resultFromHandler = result.result();
      assertNull(resultFromHandler);
    });
  }
}
