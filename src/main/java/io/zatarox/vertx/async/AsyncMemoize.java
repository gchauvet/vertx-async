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

public interface AsyncMemoize<I, O> {

    /**
     * @param argument Argument used as key.
     * @return data associated to argument.
     */
    O get(final I argument);

    /**
     * @param argument Argument used as key
     * @return True if result associated to argument have been removed.
     */
    boolean unset(final I argument);

    /**
     * Clean cache
     */
    void clear();
    
    /**
     * @return True if is empty
     */
    boolean isEmpty();

}
