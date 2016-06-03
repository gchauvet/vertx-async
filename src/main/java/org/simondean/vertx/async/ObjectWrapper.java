package org.simondean.vertx.async;

public class ObjectWrapper<T> {

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
