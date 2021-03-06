# vertx-async
[![Build Status via Travis CI](https://travis-ci.org/gchauvet/vertx-async.svg?branch=master)](https://travis-ci.org/gchauvet/vertx-async)
[![Coverage Status](https://coveralls.io/repos/github/gchauvet/vertx-async/badge.svg?branch=master)](https://coveralls.io/github/gchauvet/vertx-async?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8d5ba040f44c44c48d6af3639a5aef35)](https://www.codacy.com/app/gchauvet/vertx-async?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gchauvet/vertx-async&amp;utm_campaign=Badge_Grade)
[![Dependency Status](https://www.versioneye.com/user/projects/576e250f7bc681003c4900b1/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/576e250f7bc681003c4900b1)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.zatarox/vertx-async/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.zatarox/vertx-async)

vertx-async is a portage of caolan/async nodejs module to [Vert.x](http://vertx.io/) framework that provides helpers methods for common async patterns.

Async provides many methods that include the usual 'functional' suspects (`map`, `reduce`, `filter`, `each`…) as well as some common patterns for asynchronous control flow (`parallel`, `series`, `waterfall`…). All these functions assume you follow the  [vert.x convention](http://vertx.io/docs/vertx-core/java/#_don_t_call_us_we_ll_call_you).

<p align="center">
<img style="width:100%" src="https://i.chzbgr.com/full/5068754944/hBECA40C8"></a>
</p>

## Installation

vertx-async is available on maven central repository and OSSRH repository.

## Quick Examples

### Each

#### On a collection
```java
    @Override
    public void start(final Future<Void> startFuture) {
        AsyncFactorySingleton.getInstance().createCollections(context)
        .each(IntStream.iterate(0, i -> i + 1).limit(100).boxed().collect(Collectors.toList()), (item, handler) -> {
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
        AsyncFactorySingleton.getInstance().createCollections(context)
        .each(IntStream.iterate(0, i -> i + 1).limit(100).boxed().collect(Collectors.toMap(p -> p.toString(), Function.identity())), (item, handler) -> {
            System.out.println(item.getKey() + " -> " + item.getValue());
            handler.handle(DefaultAsyncResult.succeed());
        }, e -> {
            System.out.println("done.");
            startFuture.complete(e.result());
        });
    }
```

There are many more functions available so take a look at the wiki for a full list (work in progress) . This README aims to be comprehensive, so if you feel anything is missing please create a GitHub issue for it.

### Multiple callbacks

Make sure to always calling the callback handler once, instead of a `return` procedural programming statement style, otherwise you will cause multiple callbacks and unpredictable behavior in many cases.

## Documentation

See our wiki (:construction:).

### Collections
|   |   |   |   |   |   |   |   |   |   |   |
|---|---|---|---|---|---|---|---|---|---|---|
| each  | map  | filter  | reject  | reduce  | transform  | detect  | sort  | some  | every  | concat |

### Control Flow
|   |   |   |   |   |   |   |   |
|---|---|---|---|---|---|---|---|
| series  | parallel  | whilst  | until  | during | forever  | waterfall  | seq |
| retry | queue | applyEach (each) | times | race | cargo |  |  |

# Utils
|   |   |   |   |   |   |   |   |
|---|---|---|---|---|---|---|---|
| asyncify | constant | memoize | timeout |   |   |   |   |
