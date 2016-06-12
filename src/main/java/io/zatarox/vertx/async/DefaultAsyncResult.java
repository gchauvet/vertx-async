/*
 * The MIT License
 *
 * Copyright 2016 Guillaume Chauvet.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
