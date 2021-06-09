# Dependencies

## Repositories

All Stream Android libraries are available from MavenCentral, with some of their transitive dependencies hosted on Jitpack. 

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

To add the low-level Chat client library to your app, open your module's `build.gradle` script and add the following:

```groovy
dependencies {
    implementation "io.getstream:stream-chat-android-client:$stream_version"
}
```

## Offline support

To use offline support in your application, add the following dependency:

```kotlin
dependencies {
    implementation "io.getstream:stream-chat-android-offline:$stream_version"
}
```

This also adds the client library automatically.

## UI Components

To use UI components in your application, add the following dependency:

```kotlin
dependencies {
    implementation "io.getstream:stream-chat-android-ui-components:$stream_version"
}
```

Adding the UI Components library as a dependency will automatically include the client and offline libraries as well.
