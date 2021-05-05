---
id: offlineSetup
title: Setup
sidebar_position: 1
---

The offline library exposes easy to use LiveData/StateFlow objects for messages, reads, typing, members, watchers and more.
It also adds support for offline chat. This means you can send messages, reactions and even create channels while you're offline. When the user comes back online, the library will automatically recover lost events and retry sending messages.
The offline storage also enables it to implement optimistic UI updates.

> **Optimistic UI Updates explained**: If you send a message using the offline support lib it will immediately update the underlying LiveData objects and the connected UI. The actual API call happens in the background. Especially in high latency or unreliable network conditions this massively improves the perceived performance of the chat interface.

## Adding Dependencies

Offline package is build on top of the Client package. You need to add the following dependency if you aim to use Stream SDK Offline package:
```kotlin
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    // for Kotlin projects
    kotlinOptions {
        jvmTarget = '1.8'
    }
}

dependencies {
    implementation "io.getstream:stream-chat-android-offline:$stream_version"
}
```
See the [releases](https://github.com/GetStream/stream-chat-android/releases) page for the latest version number.

## Initializing SDK
As a first step, you need to initialize each of the SDK components. Most of the times, this will happen only once in the _Application_ class:
```kotlin
val chatClient = ChatClient.Builder(apiKey, appContext).build()

val chatDomain = ChatDomain.Builder(appContext, chatClient)
    .offlineEnabled()
    .userPresenceEnabled()
    .build()
```
Every _Builder_ expose multiple methods that allow different SDK configuration.
