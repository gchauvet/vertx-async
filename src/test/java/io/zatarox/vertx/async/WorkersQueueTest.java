/*
 * Copyright 2016 Guillaume Chauvet.
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

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public final class WorkersQueueTest {
    
    /**
     * Limits
     */
    private static final int TIMEOUT_LIMIT = 1000;
    private static final int REPEAT_LIMIT = 100;

    private WorkersQueue queue;

    @Rule
    public RepeatRule repeater = new RepeatRule();
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void setUp() {
        queue = new WorkersQueue();
        assertNotNull(queue);
        assertEquals(0, queue.getRunning());
    }
    
    @Test(timeout = WorkersQueueTest.TIMEOUT_LIMIT)
    @Repeat(WorkersQueueTest.REPEAT_LIMIT)
    public void executeEmptyQueue() {
        rule.vertx().runOnContext(queue);
        assertEquals(0, queue.getRunning());
    }
    
    @Test(timeout = WorkersQueueTest.TIMEOUT_LIMIT)
    @Repeat(WorkersQueueTest.REPEAT_LIMIT)
    public void executeOneTaskSucceedInQueue(final TestContext context) {
        final Async async = context.async();
        queue.add(instance -> {
            context.assertNotNull(instance);
            async.complete();
        });
        assertTrue(queue.add(t -> {
            context.assertEquals(1, queue.getRunning());
            rule.vertx().setTimer(100, event -> {
                t.handle(DefaultAsyncResult.succeed());
            });
        }, event -> {
            context.assertTrue(event.succeeded());
            context.assertEquals(0, queue.getRunning());
        }));
        rule.vertx().runOnContext(queue);
    }
    
    @Test(timeout = WorkersQueueTest.TIMEOUT_LIMIT)
    @Repeat(WorkersQueueTest.REPEAT_LIMIT)
    public void executeTwoTaskSucceedWithOneWorker(final TestContext context) {
        final Async async = context.async();
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final WorkersQueue queue = new WorkersQueue(1);
        queue.add(instance -> {
            context.assertNotNull(instance);
            async.complete();
        });
        assertTrue(queue.add(t -> {
            context.assertEquals(1, queue.getRunning());
            rule.vertx().setTimer(100, event -> {
                t.handle(DefaultAsyncResult.succeed());
            });
        }, event -> {
            context.assertTrue(event.succeeded());
            context.assertEquals(1, queue.getRunning());
        }));
        assertTrue(queue.add(t -> {
            context.assertEquals(2, queue.getRunning());
            rule.vertx().setTimer(200, event -> {
                t.handle(DefaultAsyncResult.succeed());
            });
        }, event -> {
            context.assertTrue(event.succeeded());
            context.assertEquals(0, queue.getRunning());
        }));
        rule.vertx().runOnContext(queue);
    }
    
    @Test(timeout = WorkersQueueTest.TIMEOUT_LIMIT)
    @Repeat(WorkersQueueTest.REPEAT_LIMIT)
    public void executeTwoTaskSucceedWithDefaultNumberWorkers(final TestContext context) {
        final Async async = context.async();
        queue.add(instance -> {
            context.assertNotNull(instance);
            async.complete();
        });
        assertTrue(queue.add(t -> {
            context.assertEquals(1, queue.getRunning());
            rule.vertx().setTimer(100, event -> {
                t.handle(DefaultAsyncResult.succeed());
            });
        }, event -> {
            context.assertTrue(event.succeeded());
            context.assertEquals(1, queue.getRunning());
        }));
        assertTrue(queue.add(t -> {
            context.assertEquals(2, queue.getRunning());
            rule.vertx().setTimer(200, event -> {
                t.handle(DefaultAsyncResult.succeed());
            });
        }, event -> {
            context.assertTrue(event.succeeded());
            context.assertEquals(0, queue.getRunning());
        }));
        rule.vertx().runOnContext(queue);
    }
    
    @Test(timeout = WorkersQueueTest.TIMEOUT_LIMIT)
    @Repeat(WorkersQueueTest.REPEAT_LIMIT)
    public void executeOneTaskFailedInQueue(final TestContext context) {
        final Async async = context.async();
        queue.add(instance -> {
            context.assertNotNull(instance);
            async.complete();
        });
        assertTrue(queue.add(t -> {
            context.assertEquals(1, queue.getRunning());
            rule.vertx().setTimer(100, event -> {
                t.handle(DefaultAsyncResult.fail(new IllegalArgumentException()));
            });
        }, event -> {
            context.assertFalse(event.succeeded());
            context.assertTrue(event.cause() instanceof IllegalArgumentException);
            context.assertEquals(0, queue.getRunning());
        }));
        rule.vertx().runOnContext(queue);
    }
    
}
