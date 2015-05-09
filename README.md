# vertx-async

vertx-async is a [Vert.x](http://vertx.io/) module that provides helper methods for common async patterns.
It helps avoid call back hell and is inspired by the API of the popular [async](https://www.npmjs.com/package/async)
node.js module.

# Using

The module is available on Maven Central:

``` xml
<dependency>
  <groupId>org.simondean.vertx</groupId>
  <artifactId>vertx-async</artifactId>
  <version>0.1.0</version>
  <scope>provided</scope>
</dependency>
```

The module then needs to be added to the includes field of your mod.json:

``` json
  "includes": "org.simondean.vertx~vertx-async~0.1.0"
```

The patterns are all available as static methods on the `org.simondean.vertx.async.Async` class.

# Patterns

## Series

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
        handler.handle(new DefaultFutureResult(result.cause()));
        return;
      }

      List<String> resultList = result.result();
      doSomethingWithTheResults(resultList);

      handler.handle(new DefaultFutureResult(resultList));
    });
}
```
