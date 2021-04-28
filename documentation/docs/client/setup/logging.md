---
id: clientLogging
title: Logging
sidebar_position: 4
---

# Logging

## Enabling logs

SDK logs are disabled by default. You can enable logs and set a log level when initializing `ChatClient`:

```kotlin
val client = ChatClient.Builder("apiKey", context)
    .logLevel(ChatLogLevel.ALL)
    .build()
```

## Intercepting logs

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

## Finding logs

All SDK log tags have the `Chat:` prefix, so you can filter for that those in the logs:

```bash
adb logcat com.your.package | grep "Chat:"
```

Here's a set of useful tags for debugging network communication:

- `Chat:Http`
- `Chat:Events`
- `Chat:SocketService`
