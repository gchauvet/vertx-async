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
import org.junit.Test;
import static org.junit.Assert.*;

public final class AsyncResultHandlerWrapperTest {

    @Test(timeout = 100)
    public void testWrapperSucceed() {
        final AsyncResultHandlerWrapper<Void, Void> instance = new AsyncResultHandlerWrapper<>((AsyncResult<Void> event) -> {
             assertTrue(event.succeeded());
             assertFalse(event.failed());
             assertNull(event.cause());
             assertNull(event.result());
        });
        instance.handle(DefaultAsyncResult.succeed());
    }
    
    @Test(timeout = 100)
    public void testWrapperFailed() {
        final AsyncResultHandlerWrapper<Void, Void> instance = new AsyncResultHandlerWrapper<>((AsyncResult<Void> event) -> {
             assertFalse(event.succeeded());
             assertTrue(event.failed());
             assertTrue(event.cause() instanceof UnsupportedOperationException);
             assertNull(event.result());
        });
        instance.handle(DefaultAsyncResult.fail(new UnsupportedOperationException()));
    }
    
}
