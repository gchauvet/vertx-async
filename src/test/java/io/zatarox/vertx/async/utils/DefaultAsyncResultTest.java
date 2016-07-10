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
package io.zatarox.vertx.async.utils;

import io.vertx.core.AsyncResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(VertxUnitRunner.class)
public final class DefaultAsyncResultTest {

    /**
     * Limits
     */
    private static final int TIMEOUT_LIMIT = 1000;
    private static final int REPEAT_LIMIT = 100;

    @Rule
    public RepeatRule repeater = new RepeatRule();
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test(timeout = DefaultAsyncResultTest.TIMEOUT_LIMIT)
    @Repeat(DefaultAsyncResultTest.REPEAT_LIMIT)
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

    @Test(timeout = DefaultAsyncResultTest.TIMEOUT_LIMIT)
    @Repeat(DefaultAsyncResultTest.REPEAT_LIMIT)
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

    @Test(timeout = DefaultAsyncResultTest.TIMEOUT_LIMIT, expected = IllegalArgumentException.class)
    @Repeat(DefaultAsyncResultTest.REPEAT_LIMIT)
    public void unvalidFailed(final TestContext context) {
        final Async async = context.async();
        final AsyncResult<Void> instance = DefaultAsyncResult.fail((Throwable) null);
        rule.vertx().runOnContext((Void event) -> {
            context.assertNull(instance);
            context.fail();
            async.complete();
        });
    }

    @Test(timeout = DefaultAsyncResultTest.TIMEOUT_LIMIT)
    @Repeat(DefaultAsyncResultTest.REPEAT_LIMIT)
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

    @Test(timeout = DefaultAsyncResultTest.TIMEOUT_LIMIT)
    @Repeat(DefaultAsyncResultTest.REPEAT_LIMIT)
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
