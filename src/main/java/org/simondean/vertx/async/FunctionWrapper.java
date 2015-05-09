package org.simondean.vertx.async;

public class FunctionWrapper<T> {
  private T f;

  public void wrap(T f) {
    this.f = f;
  }

  public T f() {
    return f;
  }
}
