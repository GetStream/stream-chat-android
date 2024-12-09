# Official Android SDK for [Stream Chat](https://getstream.io/chat/sdk/android/)

> This is the official Android SDK for [Stream Chat](https://getstream.io/chat/sdk/android/), a service for building chat and messaging applications. This library includes both a low-level chat SDK and a set of reusable UI components. Most users start with the UI components, and fall back to the lower level API when they want to customize things.

<br />
<p align="left">
  <a href="https://github.com/GetStream/stream-chat-android/actions"><img src="https://github.com/GetStream/stream-chat-android/workflows/Build%20and%20test/badge.svg" /></a>
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/GetStream/stream-chat-android/releases"><img src="https://img.shields.io/github/v/release/GetStream/stream-chat-android" /></a>
</p>

### 🔗 Quick Links

* [Register](https://getstream.io/chat/trial/): Create an account and get an API key for Stream Chat
> Small teams and individuals can also apply for a [Maker Account](https://getstream.io/maker-account/) that allows you to use Startup Plan for free.

#### Tutorials
* [Chat Tutorial](https://getstream.io/tutorials/android-chat/#kotlin): Learn the basics of the SDK by by building a simple messaging app (Kotlin or Java)

#### Sample Apps
* [UI Components sample app](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components-sample): Full messaging app with threads, reactions, optimistic UI updates and offline storage
* [Compose UI Components sample app](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-compose-sample): Messaging sample app built with Jetpack Compose!
* [Android Samples Repo](https://github.com/GetStream/Android-Samples): This repository contains sample projects, guides, tutorials, and links to helpful resources to help you get started with Android Stream SDK.

#### Documentation
* [Client Documentation](https://getstream.io/chat/docs/android/?language=kotlin)
* [UI Components Documentation](https://getstream.io/chat/docs/sdk/android/)
* [Compose UI Components Documentation](https://getstream.io/chat/docs/sdk/android/compose/overview/)
* [UI Team Planning](https://github.com/orgs/GetStream/projects/6): UI Team public project management board and milestone overview
* [Core Team Planning](https://github.com/orgs/GetStream/projects/7): Core Team public project management board and milestone overview

### V5 Migration Guide

For upgrading from V4 to V5, please refer to the [V5 Migration Guide](https://getstream.io/chat/docs/sdk/android/client/guides/chatdomain-migration/)

### Changelog

Check out the [CHANGELOG](https://github.com/GetStream/stream-chat-android/releases) to see the changes and improvements in each of our releases.

## Getting started
There are only **3 steps** to get started with Stream!

1. Add Stream chat SDK dependency to your project.
2. Set up Stream chat client to communicate to the API.
3. Connect our components to ViewModels to show data.

> Note: Alternatively, you can skip using our UI components and build your custom UI powered by our core SDK.

Let's cover some of these steps.

### Add dependency
Add one of the four packages below to your dependencies for your `module/app` level `build.gradle` file:

```groovy
repositories {
  google()
  mavenCentral()
  maven { url "https://jitpack.io" }
  jcenter()
}

dependencies {
  // Client + offline + Compose
  implementation "io.getstream:stream-chat-android-compose:$stream_version"
  // Client + offline + UI components
  implementation "io.getstream:stream-chat-android-ui-components:$stream_version"
  // Client + offline
  implementation "io.getstream:stream-chat-android-offline:$stream_version"
  // Client only
  implementation "io.getstream:stream-chat-android-client:$stream_version"
}
```

### Setup API Client

First, you need to instantiate a chat client. The Chat client will manage API calls, event handling, and manage the WebSocket connection to Stream Chat servers. You should only create the client once and reuse it across your application.

```kotlin
val apiKey = "{{ api_key }}"
val token = "{{ chat_user_token }}"

val client = ChatClient.Builder(apiKey, applicationContext).build()
```

Next, you need to authenticate and connect the user.
```kotlin
val user = User(
      id = "summer-brook-2",
      name = "Paranoid Android",
      image = "https://bit.ly/2TIt8NR"
)
client.connectUser(
    user = user,
    token = token, // or client.devToken(userId); if auth is disabled for your app
).enqueue { result ->
    if (result.isSuccess) {
        // Handle success
    } else {
        // Handler error
    }
}
```

The user token is typically provided by your backend when you login or register in the app. If authentication is disabled for your app, you can also use a `ChatClient#devToken` to generate an insecure token for development. Of course, you should never launch into production with authentication disabled.

There is also a [Stream Token Generator](https://getstream.io/chat/docs/react/token_generator/) which can help you to generate user tokens for prototyping and debugging; usually by hardcoding this into your application or passing it as an environment value at initialization.

For more complex token generation and expiration examples, have a look at [Token Expiration](https://getstream.io/chat/docs/android/tokens_and_authentication/#token-expiration).

### Offline storage

To add data persistence you can provide the `ChatClient.Builder` with an instance of `StreamOfflinePluginFactory`.

```kotlin
val offlinePluginFactory = StreamOfflinePluginFactory(
  config = Config(
    backgroundSyncEnabled = true,
    userPresence = true,
    persistenceEnabled = true,
    uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
  ),
  appContext = applicationContext,
)

val client = ChatClient.Builder(apiKey, applicationContext)
    .withPlugin(offlinePluginFactory)
    .build()
```

### Logging

During development, you might want to see the SDK logs to investigate a specific area of interest. Let's see how it can be done!

> Please take into account that the SDK will write no logs by default.

#### Change Logging Level

To enable logging information, you can change the default log level when constructing the client.

```kotlin
val client = ChatClient.Builder(apiKey, applicationContext)
    // Change log level
    .logLevel(ChatLogLevel.ALL)
    .build()
```

#### Custom Logger

You can handle the log messages directly instead of have them written to Logcat, this is very convenient if you use an error tracking tool or if you want to centralize your logs into one facility.

```kotlin
val loggerHandler = object : ChatLoggerHandler {
    //...
}
val client = ChatClient.Builder(apiKey, applicationContext)
    // Enable logs
    .logLevel(ChatLogLevel.ALL)
    // Provide loggerHandler instance
    .loggerHandler(loggerHandler)
    .build()
```

## Sample apps

### 🏗️ Jetpack Compose based

Our Jetpack Compose implementation comes with its own [example app](/stream-chat-android-compose-sample), which you can play with to see how awesome Compose is.

To run the sample app, start by cloning this repo:

```shell
git clone git@github.com:GetStream/stream-chat-android.git
```

Next, open [Android Studio](https://developer.android.com/studio) and open the newly created project folder. You'll want to run the [`stream-chat-android-compose-sample`](/stream-chat-android-compose-sample) module.

### 📲 XML based

However, if you're still using XML due to technical limitations, our UI Components SDK includes a fully functional [example app](/stream-chat-android-ui-components-sample) featuring threads, reactions, typing indicators, optimistic UI updates and offline storage. To run the sample app, start by cloning this repo:

```shell
git clone git@github.com:GetStream/stream-chat-android.git
```

Next, open [Android Studio](https://developer.android.com/studio) and open the newly created project folder. You'll want to run the [`stream-chat-android-ui-components-sample`](/stream-chat-android-ui-components-sample) app.

## Contributing

### Code conventions

Make sure that you run the following commands before committing your code:
- `./gradlew spotlessApply -q`
- `./gradlew detekt`

### Public API changes

- run `./gradlew apiCheck -q`

### Running tests

- run `./gradlew test`
