package org.simondean.vertx.async.internal;

import org.simondean.vertx.async.DefaultAsyncResult;
import org.simondean.vertx.async.EachBuilder;
import org.simondean.vertx.async.ObjectWrapper;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Vertx;

import java.util.Iterator;
import java.util.function.BiConsumer;

public class EachBuilderImpl<T> implements EachBuilder {
  private final Iterable<T> iterable;
  private final BiConsumer<T, AsyncResultHandler<Void>> each;

  public EachBuilderImpl(Iterable<T> iterable, BiConsumer<T, AsyncResultHandler<Void>> each) {
    this.iterable = iterable;
    this.each = each;
  }

  @Override
  public void run(Vertx vertx, AsyncResultHandler<Void> handler) {
    final ObjectWrapper<Boolean> failed = new ObjectWrapper<>(false);

    for (Iterator<T> iterator = iterable.iterator(); iterator.hasNext();) {
      vertx.runOnContext(aVoid -> {
        each.accept(iterator.next(), result -> {
          if (result.failed()) {
            if (failed.getObject().booleanValue() == false) {
              handler.handle(DefaultAsyncResult.fail(result));
              failed.setObject(true);
            }

            return;
          }
        });
      });

      if (failed.getObject().booleanValue() == true) {
        return;
      }
    }
  }
}
