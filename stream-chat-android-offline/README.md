# Stream Chat Android Offline

This module adds offline support and provides LiveData APIs to Stream's Chat SDK. Check out [the documentation for Offline](https://getstream.io/chat/docs/livedata/?language=kotlin) on our website and [the main README](../README.md) in this repo for more info.

This library supports both Kotlin and Java usage, but we strongly recommend using Kotlin.

## Setup

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.getstream:stream-chat-android-offline:$stream_version"
}

android {
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}
```

> For the latest version, check the [Releases page](https://github.com/GetStream/stream-chat-android/releases).

## Offline

Offline support is essential for a good chat user experience, as mobile networks tend to lose connection frequently. This package ensures you can still send messages, reactions and create new channels while offline.

It also implements a retry strategy to resend messages, reactions and channels.

## LiveData

Stream's chat API exposes a few dozen events that all update the chat state Messages can be created, updated and removed. Channels can be updated, muted, deleted, members can be added to them.

The end result is that you need a lot of boilerplate code to keep your local chat state up to date. This library handles all this logic for you and simply exposes LiveData objects to observe the current state.
