package org.simondean.vertx.async;

public interface RetryTimesBuilder<T> {

    Retry<T> times(int times);
}
