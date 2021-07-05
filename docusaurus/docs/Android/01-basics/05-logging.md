# Logging

SDK logs are disabled by default. You can enable logs and set a log level when initializing `ChatClient`.

You can set logs at the following levels:

- `ChatLogLevel.ALL` to see all log entries.
- `ChatLogLevel.DEBUG` to see debug, warning, and error entries.
- `ChatLogLevel.WARN` to see warning and error entries.
- `ChatLogLevel.ERROR` to see error entries.
- `ChatLogLevel.NOTHING` to not show any logs.

```kotlin
val client = ChatClient.Builder("apiKey", context)
    .logLevel(ChatLogLevel.ALL)
    .build()
```

It's recommended to only enable logging in development builds.

## Intercepting Logs

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

## Filtering Logs

All SDK log tags have `Chat:` as a prefix that you can use when filtering logs.

```bash
adb logcat com.your.package | grep "Chat:"
```

Here's a set of useful tags for debugging network communication:

- `Chat:Http` to see HTTP requests made from the `ChatClient` and the responses returned by Stream.
- `Chat:Events` to see a list of [events](https://getstream.github.io/stream-chat-android/stream-chat-android-client/stream-chat-android-client/io.getstream.chat.android.client.events/-chat-event/index.html) that the `ChatClient` emits.
- `Chat:SocketService` to see socket related events.
