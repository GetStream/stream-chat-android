---
id: clientInterceptingLogs
title: Intercepting Logs
sidebar_position: 2
---

# Intercepting Logs

If you need to intercept logs, you can also pass in your own `ChatLoggerHandler`:

```kotlin
val client = ChatClient.Builder("apiKey", context)
    .logLevel(ChatLogLevel.ALL)
    .loggerHandler(object : ChatLoggerHandler {
        override fun logD(tag: Any, message: String) {
            // custom logging
        }
        ...
     })
     .build()
```
