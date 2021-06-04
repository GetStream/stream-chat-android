---
id: offline-setup
title: Setup
sidebar_position: 1
---

The offline library exposes easy to use LiveData/StateFlow objects for messages, reads, typing, members, watchers and more.
It also adds support for offline chat. This means you can send messages, reactions and even create channels while you're offline. When the user comes back online, the library will automatically recover lost events and retry sending messages.
The offline storage also provides support for implementing optimistic UI updates.

> **Optimistic UI Updates explained**: If you send a message using the offline support lib it will immediately update the underlying LiveData objects and the connected UI. The actual API call happens in the background. This tends to improve a user's perceived performance of the chat interface. This is especially for applications running in high latency or unreliable network conditions such as mobile applications.

## Adding Dependencies

Offline package is build on top of the Client package. You need to add the following dependency if you aim to use Stream SDK Offline package:

```kotlin
dependencies {
    implementation "io.getstream:stream-chat-android-offline:$stream_version"
}
```

See the [releases](https://github.com/GetStream/stream-chat-android/releases) page for the latest version number.

## Initializing SDK
First, you need to initialize each of the SDK components. In most cases, you will only need to do this once in the _Application_ class:

```kotlin
val chatClient = ChatClient.Builder(apiKey, appContext).build()

val chatDomain = ChatDomain.Builder(appContext, chatClient)
    .offlineEnabled()
    .userPresenceEnabled()
    .build()
```

Every _Builder_ exposes methods for SDK configuration.
