package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.unit.examples.WaterfallExample;


import static org.junit.Assert.*;

public class WaterfallExampleTest {
  @Test
  public void itHandlesSuccess() {
    WaterfallExample example = new WaterfallExample(true);

    example.waterfallExample(result -> {
      assertNotNull(result);
      assertTrue(result.succeeded());

      Integer resultsFromHandler = result.result();
      assertNotNull(resultsFromHandler);
      assertEquals(42, (int) resultsFromHandler);
      Integer resultsFromExample = example.result();
      assertNotNull(resultsFromExample);
      assertEquals(42, (int) resultsFromExample);
    });
  }

  @Test
  public void itHandlesFailure() {
    WaterfallExample example = new WaterfallExample(false);

    example.waterfallExample(result -> {
      assertNotNull(result);
      assertFalse(result.succeeded());

      Integer resultsFromHandler = result.result();
      assertNull(resultsFromHandler);
      Integer resultsFromExample = example.result();
      assertNull(resultsFromExample);
    });
  }
}
