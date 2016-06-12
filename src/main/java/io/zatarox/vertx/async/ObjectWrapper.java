package io.zatarox.vertx.async;

final class ObjectWrapper<T> {

    private T object;

    public ObjectWrapper() {
    }

    public ObjectWrapper(T object) {
        this.object = object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }
}
