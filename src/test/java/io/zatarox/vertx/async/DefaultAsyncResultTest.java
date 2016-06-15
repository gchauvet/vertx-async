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
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public final class DefaultAsyncResultTest {
    
    /**
     * Timelimit
     */
    private static final int LIMIT = 1000;
    
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test(timeout = DefaultAsyncResultTest.LIMIT)
    public void validFailedException(final TestContext context) {
        final Async async = context.async();
        final AsyncResult<Void> instance = DefaultAsyncResult.fail(new UnsupportedOperationException());
        rule.vertx().runOnContext((Void event) -> {
            context.assertFalse(instance.succeeded());
            context.assertTrue(instance.failed());
            context.assertTrue(instance.cause() instanceof UnsupportedOperationException);
            async.complete();
        });
    }
    
        @Test(timeout = DefaultAsyncResultTest.LIMIT)
    public void validFaileAsyncResult(final TestContext context) {
        final Async async = context.async();
        final AsyncResult<Void> instance = DefaultAsyncResult.fail(DefaultAsyncResult.fail(new UnsupportedOperationException()));
        rule.vertx().runOnContext((Void event) -> {
            context.assertFalse(instance.succeeded());
            context.assertTrue(instance.failed());
            context.assertTrue(instance.cause() instanceof UnsupportedOperationException);
            async.complete();
        });
    }
    
    @Test(timeout = DefaultAsyncResultTest.LIMIT, expected = IllegalArgumentException.class)
    public void unvalidFailed(final TestContext context) {
        final Async async = context.async();
        final AsyncResult<Void> instance = DefaultAsyncResult.fail((Throwable) null);
        rule.vertx().runOnContext((Void event) -> {
            context.assertNull(instance);
            context.fail();
            async.complete();
        });
    }
    
    @Test(timeout = DefaultAsyncResultTest.LIMIT)
    public void validVoidSuccess(final TestContext context) {
        final Async async = context.async();
        final AsyncResult<Void> instance = DefaultAsyncResult.succeed();
        rule.vertx().runOnContext((Void event) -> {
            context.assertTrue(instance.succeeded());
            context.assertFalse(instance.failed());
            context.assertNull(instance.cause());
            context.assertNull(instance.result());
            async.complete();
        });
    }
    
    @Test(timeout = DefaultAsyncResultTest.LIMIT)
    public void validValueSuccess(final TestContext context) {
        final Async async = context.async();
        final AsyncResult<Integer> instance = DefaultAsyncResult.succeed(73);
        rule.vertx().runOnContext((Void event) -> {
            context.assertTrue(instance.succeeded());
            context.assertFalse(instance.failed());
            context.assertNull(instance.cause());
            context.assertEquals(73, instance.result());
            async.complete();
        });
    }   
}
