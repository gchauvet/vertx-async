# vertx-async

vertx-async is a [Vert.x](http://vertx.io/) module that provides helper methods for common async patterns.
It helps avoid call back hell and is based on the API of the popular [async](https://www.npmjs.com/package/async)
node.js module.

# Using

The module is available on Maven Central:

``` xml
<dependency>
  <groupId>org.simondean</groupId>
  <artifactId>vertx-async</artifactId>
  <version>0.1.0</version>
</dependency>
```

The patterns are all available as static methods on the `org.simondean.vertx.async.Async` class.

# Patterns

## Series

``` java
public void doSomething(AsyncResultHandler<List<Object>> handler) {
  Async.series<Object>()
    .task(taskHandler -> {
      Object result = getSomeResult();
      taskHandler.handle(new DefaultFutureResult(result));
    })
    .task(taskHandler -> {
      someAsyncMethodThatTakesAHandler(taskHandler);
    })
    .run(result -> {
      if (result.failed()) {
        handler.handle(new DefaultFutureResult(result.cause()));
        return;
      }

      handler.handle(new DefaultFutureResult(result.result()));
    });
  }
```
