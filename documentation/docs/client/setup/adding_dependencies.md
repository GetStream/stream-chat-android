---
id: clientAddingDependencies
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

Open up the app module's `build.gradle` script and make the following changes:

```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation "io.getstream:stream-chat-android-client:$stream_version"
}
```

> For the latest version, check the [Releases page](https://github.com/GetStream/stream-chat-android/releases).
