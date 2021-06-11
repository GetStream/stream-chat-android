# Dependencies

## Repositories

The Stream Android libraries are available from MavenCentral, with some of their dependencies hosted on Jitpack. 

To add Stream dependencies, update your repositories in the project level `build.gradle` file to include these two repositories:

```groovy
allprojects {
    repositories {
        mavenCentral()
        maven { url "https://jitpack.io" }
    }
}
```

Check the [Releases page](https://github.com/GetStream/stream-chat-android/releases) for the latest version and the changelog. [![Latest version badge](https://img.shields.io/github/v/release/GetStream/stream-chat-android)](https://github.com/GetStream/stream-chat-android/releases) 

## Client

<!-- TODO: Add brief description about what the client module contains -->

To add the low-level Chat client library to your app, open your module's `build.gradle` script and add the following:

```groovy
dependencies {
    implementation "io.getstream:stream-chat-android-client:$stream_version"
}
```

## Offline support

<!-- TODO: Review this marketing description -->

The offline library exposes easy to use LiveData/StateFlow objects for messages, reads, typing, members, watchers and more.
It also adds support for offline chat. This means you can send messages, reactions and even create channels while you're offline. When the user comes back online, the library will automatically recover lost events and retry sending messages.
The offline storage also provides support for implementing optimistic UI updates.

:::note Optimistic UI Updates explained
If you send a message using the offline support lib it will immediately update the underlying LiveData objects and the connected UI. The actual API call happens in the background. This tends to improve a user's perceived performance of the chat interface. This is especially for applications running in high latency or unreliable network conditions such as mobile applications.
:::

Offline package is built on top of the Client package. You need to add the following dependency to use the Offline package:

```kotlin
dependencies {
    implementation "io.getstream:stream-chat-android-offline:$stream_version"
}
```

## UI Components

<!-- TODO: Add brief description about what the UI Components module contains -->

The UI package is built on top of the Client and Offline packages. To use the UI components, add the following dependency:

```kotlin
dependencies {
    implementation "io.getstream:stream-chat-android-ui-components:$stream_version"
}
```
