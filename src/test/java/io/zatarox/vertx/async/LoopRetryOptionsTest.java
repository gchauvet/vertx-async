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

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

public final class RetryOptionsTest {

    @Test
    public void onlyTry() {
        final RetryOptions instance = new RetryOptions(1);
        assertNull(instance.getUnit());
        assertEquals(1, instance.getTries());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeTry() {
        assertNull(new RetryOptions(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeTime() {
        assertNull(new RetryOptions(1, TimeUnit.SECONDS, -7));
    }

    @Test
    public void tryWithSeconds() {
        final RetryOptions instance = new RetryOptions(1, TimeUnit.SECONDS, 7);
        assertNotNull(instance.getUnit());
        assertNotNull(instance.getDelay());
        assertEquals(1, instance.getTries());
        assertEquals(7000, instance.getUnit().toMillis(instance.getDelay()));
    }

}
