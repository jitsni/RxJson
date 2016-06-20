RxJson
======

Reactive extensions (Observable API) for JSON

```
Observable<CharBuffer> buffers = ...

Observable.from(buffers)
    .compose(new JsonTokenTransformer())
    // Observerbale<JsonToken>
    ...
    .compose(new JsonValueTransformer())
    // Observerbale<JsonValue>
    ...
```
