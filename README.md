# vertx-async
[![Build Status via Travis CI](https://travis-ci.org/gchauvet/vertx-async.svg?branch=master)](https://travis-ci.org/gchauvet/vertx-async)
[![Coverage Status](https://coveralls.io/repos/github/gchauvet/vertx-async/badge.svg?branch=master)](https://coveralls.io/github/gchauvet/vertx-async?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/576e250f7bc681003c4900b1/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/576e250f7bc681003c4900b1)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.zatarox/vertx-async/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.zatarox/vertx-async)

vertx-async is a portage of caolan/async nodejs module to [Vert.x](http://vertx.io/) module that provides helper methods for common async patterns.

Async provides many methods that include the usual 'functional' suspects (`map`, `reduce`, `filter`, `each`…) as well as some common patterns for asynchronous control flow (`parallel`, `series`, `waterfall`…). All these functions assume you follow the  [vert.x convention](http://vertx.io/docs/vertx-core/java/#_don_t_call_us_we_ll_call_you).

<p align="center">
<img style="width:100%" src="https://i.chzbgr.com/full/5068754944/hBECA40C8"></a>
</p>

## Installation

vertx-async snapshots are available on OSSRH repository:
```xml
    <dependency>
        <groupId>io.zatarox</groupId>
        <artifactId>vertx-async</artifactId>
        <version>0.XX.XX-SNAPSHOT</version>
    </dependency>
```

## Quick Examples

### Each

#### On a collection
```java
    @Override
    public void start(final Future<Void> startFuture) {
        CollectionsAsync.each(IntStream.iterate(0, i -> i + 1).limit(100).boxed().collect(Collectors.toList()), (item, handler) -> {
            System.out.println("get " + item);
            handler.handle(DefaultAsyncResult.succeed());
        }, e -> {
            System.out.println("done.");
            startFuture.complete(e.result());
        });
    }
```

#### On a map
```java
    @Override
    public void start(final Future<Void> startFuture) {
        final Map<String, Integer> values = new HashMap<>();
        for(int i = 0; i < 50; i++) {
            values.put(Integer.toString(i), i);
        }
        
        CollectionsAsync.each(values, (item, handler) -> {
            System.out.println(item.getKey() + " -> " + item.getValue());
            handler.handle(DefaultAsyncResult.succeed());
        }, e -> {
            System.out.println("done.");
            startFuture.complete(e.result());
        });
    }
```

There are many more functions available so take a look at the wiki for a full list. This module aims to be comprehensive, so if you feel anything is missing please create a GitHub issue for it.

### Multiple callbacks

Make sure to always calling the callback instead of a `return` procedural programming statement style, otherwise you will cause multiple callbacks and unpredictable behavior in many cases.

## Documentation

See our wiki (:construction:).

### Collections
|   |   |   |   |   |   |   |   |   |   |   |
|---|---|---|---|---|---|---|---|---|---|---|
| each  | map  | filter  | reject  | reduce  | transform  | detect  | sort  | some  | every  | concat |

### Control Flow
|   |   |   |   |   |   |   |   |
|---|---|---|---|---|---|---|---|
| series  | parallel  | whilst  | until  | during:construction:  | forever  | waterfall  | seq |
| cargo:construction: | auto:construction: | autoInject:construction: | retry:construction: | retryable:construction: | iterator:construction: | times | race:construction: |
| queue:construction: | applyEach:construction: |  |  |  |  |  |  |
