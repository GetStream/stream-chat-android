# Stream Chat Android Client

This module contains the official low-level Android SDK for Stream's Chat SDK. Check out [the official documentation](https://getstream.io/chat/docs/introduction/?language=kotlin) on our website and [the main README](../README.md) in this repo for more info.

This library integrates directly with Stream Chat APIs and does not include state handling or UI. See [the main README](../README.md) for details about the other available SDKs.

This library supports both Kotlin and Java usage, but we strongly recommend using Kotlin.

## Setup

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.getstream:stream-chat-android-client:$stream_version"
}

android {
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}
```

> For the latest version, check the [Releases page](https://github.com/GetStream/stream-chat-android/releases).
