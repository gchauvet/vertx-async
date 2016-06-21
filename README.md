# vertx-async
[![Build Status via Travis CI](https://travis-ci.org/gchauvet/vertx-async.svg?branch=master)](https://travis-ci.org/gchauvet/vertx-async)
[![Coverage Status](https://coveralls.io/repos/github/gchauvet/vertx-async/badge.svg?branch=master)](https://coveralls.io/github/gchauvet/vertx-async?branch=master)

vertx-async is a portage of caolan/async nodejs module to [Vert.x](http://vertx.io/) module that provides helper methods for common async patterns.

Async provides many methods that include the usual 'functional' suspects (`map`, `reduce`, `filter`, `each`…) as well as some common patterns for asynchronous control flow (`parallel`, `series`, `waterfall`…). All these functions assume you follow the vert.x convention of providing a single callback as the last argument of your asynchronous function -- a callback which expects an Error as its first argument -- and calling the callback once.

## Quick Examples

TODO

There are many more functions available so take a look at the wiki for a full list. This module aims to be comprehensive, so if you feel anything is missing please create a GitHub issue for it.

### Multiple callbacks

Make sure to always `return` when calling a callback early, otherwise you will cause multiple callbacks and unpredictable behavior in many cases.

## Documentation

See our wiki (:construction:).

### Collections

* each
* map
* filter
* reject
* reduce
* transform
* detect
* sort
* some
* every
* concat

### Control Flow

* series
* parallel
* whilst :construction:
* until
* during :construction:
* forever
* waterfall
* compose :construction:
* seq :construction:
* applyEach :construction:
* queue :construction:
* cargo :construction:
* auto :construction:
* autoInject :construction:
* retry :construction:
* retryable :construction:
* iterator :construction:
* times :construction:
* race :construction:
