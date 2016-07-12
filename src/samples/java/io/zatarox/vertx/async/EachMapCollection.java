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

import io.vertx.core.*;
import io.zatarox.vertx.async.utils.DefaultAsyncResult;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EachMapCollection extends AbstractVerticle {

    @Override
    public void start(final Future<Void> startFuture) {
        AsyncCollections.each(IntStream.iterate(0, i -> i + 1).limit(100).boxed().collect(Collectors.toMap(p -> p.toString(), Function.identity())), (item, handler) -> {
            System.out.println(item.getKey() + " -> " + item.getValue());
            handler.handle(DefaultAsyncResult.succeed());
        }, e -> {
            System.out.println("done.");
            startFuture.complete(e.result());
        });
    }

}
