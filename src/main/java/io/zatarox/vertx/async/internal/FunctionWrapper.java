package io.zatarox.vertx.async.internal;

final class FunctionWrapper<T> {

    private T f;

    public void wrap(T f) {
        this.f = f;
    }

    public T f() {
        return f;
    }
}
