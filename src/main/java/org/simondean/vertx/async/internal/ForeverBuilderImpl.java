package org.simondean.vertx.async.internal;

import io.vertx.core.AsyncResult;
import org.simondean.vertx.async.Forever;
import org.simondean.vertx.async.ForeverBuilder;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class ForeverBuilderImpl implements ForeverBuilder {
  @Override
  public Forever task(Consumer<Handler<AsyncResult<Void>>> task) {
    return new ForeverImpl(task);
  }
}
