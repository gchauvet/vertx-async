# vertx-async

vertx-async is a [Vert.x](http://vertx.io/) module that provides helper methods for common async patterns.
It helps avoid call back hell and is inspired by the API of the popular [async](https://www.npmjs.com/package/async)
node.js module.

## Using

The module is available on Maven Central:

``` xml
<dependency>
  <groupId>io.zatarox.vertx</groupId>
  <artifactId>async</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The module then needs to be added to the includes field of your mod.json:

``` json
  "includes": "io.zatarox.vertx~async~0.1.0-SNAPSHOT"
```

The patterns are all available as static methods on the `io.zatarox.vertx.async.Async` class.

## Patterns

### Series

``` java
  public void seriesExample(AsyncResultHandler<List<String>> handler) {
    Async.<String>series()
      .task(taskHandler -> {
        String result = getSomeResult();
        taskHandler.handle(DefaultAsyncResult.succeed(result));
      })
      .task(taskHandler -> {
        someAsyncMethodThatTakesAHandler(taskHandler);
      })
      .run(result -> {
        if (result.failed()) {
          handler.handle(DefaultAsyncResult.fail(result.cause()));
          return;
        }

        List<String> resultList = result.result();
        doSomethingWithTheResults(resultList);

        handler.handle(DefaultAsyncResult.succeed(resultList));
      });
  }
```

### Waterfall

``` java
  public void waterfallExample(AsyncResultHandler<Integer> handler) {
    Async.waterfall()
      .<String>task(taskHandler -> {
        String result = getSomeResult();
        taskHandler.handle(DefaultAsyncResult.succeed(result));
      })
      .<Integer>task((result, taskHandler) -> {
        someAsyncMethodThatTakesAResultAndHandler(result, taskHandler);
      })
      .run(result -> {
        if (result.failed()) {
          handler.handle(DefaultAsyncResult.fail(result.cause()));
          return;
        }

        Integer resultValue = result.result();
        doSomethingWithTheResults(resultValue);

        handler.handle(DefaultAsyncResult.succeed(resultValue));
      });
  }
```

### Each

``` java
  public void eachExample(AsyncResultHandler<Void> handler) {
    List<String> list = Arrays.asList("one", "two", "three");

    Async.iterable(list)
      .each((item, eachHandler) -> {
        doSomethingWithItem(item, eachHandler);
      })
      .run(vertx, handler);
  }
```

### Retry

``` java
  public void retryExample(AsyncResultHandler<String> handler) {
    Async.retry()
      .<String>task(taskHandler -> {
        someAsyncMethodThatTakesAHandler(taskHandler);
      })
      .times(5)
      .run(result -> {
        if (result.failed()) {
          handler.handle(DefaultAsyncResult.fail(result));
          return;
        }

        String resultValue = result.result();

        doSomethingWithTheResults(resultValue);

        handler.handle(DefaultAsyncResult.succeed(resultValue));
      });
  }
```

### Forever

``` java
  public void foreverExample(AsyncResultHandler<String> handler) {
    Async.forever()
      .task(taskHandler -> {
        someAsyncMethodThatTakesAHandler(taskHandler);
      })
      .run(vertx, result -> {
        handler.handle(DefaultAsyncResult.fail(result));
      });
  }
```
