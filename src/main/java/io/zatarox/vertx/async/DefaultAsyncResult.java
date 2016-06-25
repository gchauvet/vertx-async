/*
 * Copyright 2004-2016 Guillaume Chauvet.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zatarox.vertx.async;

import io.vertx.core.AsyncResult;

public final class DefaultAsyncResult<T> implements AsyncResult<T> {

    private final Throwable cause;
    private final T result;

    public DefaultAsyncResult(Throwable cause, T result) {
        this.cause = cause;
        this.result = result;
    }

    public static <T> AsyncResult<T> succeed(T result) {
        return new DefaultAsyncResult<>(null, result);
    }

    public static AsyncResult<Void> succeed() {
        return succeed(null);
    }

    public static <T> AsyncResult<T> fail(Throwable cause) {
        if (cause == null) {
            throw new IllegalArgumentException("cause argument cannot be null");
        }

        return new DefaultAsyncResult<>(cause, null);
    }

    public static <T> AsyncResult<T> fail(AsyncResult<?> result) {
        return fail(result.cause());
    }

    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public boolean succeeded() {
        return cause == null;
    }

    @Override
    public boolean failed() {
        return cause != null;
    }
}
