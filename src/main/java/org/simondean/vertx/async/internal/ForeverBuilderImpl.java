package org.simondean.vertx.async.internal;

import org.simondean.vertx.async.Forever;
import org.simondean.vertx.async.ForeverBuilder;
import org.vertx.java.core.AsyncResultHandler;

import java.util.function.Consumer;

public class ForeverBuilderImpl implements ForeverBuilder {
  @Override
  public Forever task(Consumer<AsyncResultHandler<Void>> task) {
    return new ForeverImpl(task);
  }
}
