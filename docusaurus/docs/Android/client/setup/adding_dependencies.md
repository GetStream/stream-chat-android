---
id: client-adding-dependencies
title: Adding Dependencies
sidebar_position: 1
---

Update your repositories in the project level `build.gradle` file:

```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
```

Open the app module's `build.gradle` script and make the following changes:

```groovy
dependencies {
    implementation "io.getstream:stream-chat-android-client:$stream_version"
}
```

> For the latest version, check the [Releases page](https://github.com/GetStream/stream-chat-android/releases).
