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
package io.zatarox.vertx.async.impl;

import io.vertx.core.AsyncResult;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(VertxUnitRunner.class)
public final class AsyncResultHandlerWrapperTest {

    /**
     * Timelimit
     */
    private static final int LIMIT = 1000;
    
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    @Test(timeout = AsyncResultHandlerWrapperTest.LIMIT)
    public void testWrapperSucceed() {
        final AsyncResultHandlerWrapper<Void, Void> instance = new AsyncResultHandlerWrapper<>((AsyncResult<Void> event) -> {
             assertTrue(event.succeeded());
             assertFalse(event.failed());
             assertNull(event.cause());
             assertNull(event.result());
        });
        instance.handle(DefaultAsyncResult.succeed());
    }
    
    @Test(timeout = AsyncResultHandlerWrapperTest.LIMIT)
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
