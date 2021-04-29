---
id: stopListening
title: Stop Listening for Events
sidebar_position: 4
---

It is a good practice to unregister event handlers once they are not in use anymore. Doing so will save you from performance degradations coming from memory leaks or even from errors and exceptions (i.e. null pointer exceptions)

```kotlin
val disposable: Disposable = client.subscribe { /* ... */ } 
disposable.dispose()
```