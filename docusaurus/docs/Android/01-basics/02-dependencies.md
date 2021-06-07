# Dependencies

Update your repositories in the project level `build.gradle` file:

```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
```

## Client

Open the app module's `build.gradle` script and make the following changes:

```groovy
dependencies {
    implementation "io.getstream:stream-chat-android-client:$stream_version"
}
```

> For the latest version, check the [Releases page](https://github.com/GetStream/stream-chat-android/releases).

<a href="https://github.com/GetStream/stream-chat-android/actions"><img src="https://github.com/GetStream/stream-chat-android/workflows/Build%20and%20test/badge.svg" /></a>

## Offline support

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

## UI Components

The UI package is built on top of the Client and Offline packages. If you aim to use Stream SDK UI components you need to add dependency to the UI artifact.

```kotlin
dependencies {
    implementation "io.getstream:stream-chat-android-ui-components:$stream_version"
}

```
See the [releases](https://github.com/GetStream/stream-chat-android/releases) page for the latest version number.
If you prefer to use low-level chat Client only with Offline support library you can include those dependencies directly instead of the whole UI package.
