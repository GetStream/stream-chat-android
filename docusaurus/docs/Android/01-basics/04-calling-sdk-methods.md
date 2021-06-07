# Calling SDK Methods

Most SDK methods return a [`Call`](https://getstream.github.io/stream-chat-android/stream-chat-android-core/stream-chat-android-core/io.getstream.chat.android.client.call/-call/) object, which is a pending operation waiting to be executed.

## Running calls synchronously

You can run a `Call` synchronously, in a blocking way, using the `execute` method:

```kotlin
// Only call this from a background thread
val messageResult = channelClient.sendMessage(message).execute()
```

## Running calls asynchronously

You can run a `Call` asynchronously, on a background thread, using the `enqueue` method. The callback passed to `enqueue` will be called on the UI thread.

```kotlin
// Safe to call from the main thread
channelClient.sendMessage(message).enqueue { result: Result<Message> ->
    if (result.isSuccess) {
        val sentMessage = result.data()
    } else {
        // Handle result.error()
    }
}
```

If you are using Kotlin coroutines, you can also `await()` the result of a `Call` in a suspending way:

```kotlin
viewModelScope.launch {
    // Safe to call from any CoroutineContext
    val messageResult = channelClient.sendMessage(message).await()
}
```

## Error handling

Actions defined in a `Call` return [`Result`](https://getstream.github.io/stream-chat-android/stream-chat-android-core/stream-chat-android-core/io.getstream.chat.android.client.utils/-result/) objects. These contain either the result of a successful operation or the error that caused the operation to fail.

You can check whether a `Result` is successful or an error:

```kotlin
// Exactly one of these will be true for each Result
result.isSuccess
result.isError
```

If the result was successful, you can get the contained data with `data()`. Otherwise, you can read `error()` and handle it appropriately.

```kotlin
if (result.isSuccess) {
    // Use result.data()
} else {
    // Handle result.error()
}
```

Calling `data()` on a failed `Result` or calling `error()` on a successful `Result` will throw an `IllegalStateException`.
