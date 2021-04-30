---
id: uiSetup
title: Setup
sidebar_position: 1
---

The Android SDK enables you to build any type of chat or messaging experience for Android. It consists of 3 major components:
* [Client](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-client): The client handles all API calls and receives events.
* [Offline](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-offline): The offline lib stores the data, implements optimistic UI updates, handles network failures, and exposes LiveData or StateFlow objects that make it easy to build your own UI on top of.
* [UI](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-ui-components): The UI package includes view models and custom views for common things like a channel list, message list, message input, etc.

## Adding dependencies
UI package is build on top of the Client and Offline packages. If you aim to use Stream SDK UI components you need to add dependency to the UI artifact. 
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
    implementation "io.getstream:stream-chat-android-ui-components:$stream_version"
}

```
See the [releases](https://github.com/GetStream/stream-chat-android/releases) page for the latest version number.
If you prefer to use low-level chat Client only with Offline support library you can include those dependencies directly instead of the whole UI package.

## Initializing SDK
As a first step, you need to initialize each of the SDK components. Most of the times, this will happen only once in the _Application_ class:
```kotlin
val client = ChatClient.Builder(apiKey = "apiKey", appContext = applicationContext)
    .logLevel(ChatLogLevel.ALL)
    .build()

val domain = ChatDomain.Builder(applicationContext, client)
    .offlineEnabled()
    .build()
```
Every _Builder_ expose multiple methods that allow different SDK configuration.

## Connecting User
The next step is connecting the user:
```kotlin
val user = User(
    id = "bender",
    extraData = mutableMapOf(
        "name" to "Bender",
        "image" to "https://bit.ly/321RmWb",
    ),
)

ChatClient.instance().connectUser(user = user, token = "userToken")
    .enqueue { result ->
        if (result.isSuccess) {
            // Handle success
        } else {
            // Handle error
        }
    }
```
For more details see [Client Documentation](../client/setup/clientConnectingUser).

## ChatUI
UI components customization is supported by accessing the `ChatUI` object directly. It's initialized with default implementations - no initialization is required.
You can access `ChatUI` to customize the global behaviour of UI elements.
 * `ChatUI.fonts`: allows you to overwrite fonts
 * `ChatUI.markdown` interface to to customize the markdown parsing behaviour, useful if you want to use more markdown modules
 * `ChatUI.urlSigner` url signing logic, enables you to add authorization tokens for images, video etc
 * `ChatUI.avatarBitmapFactory` allows to generate custom bitmap for avatarView
 * `ChatUI.mimeTypeIconProvider` allows to define own icons for different mime types
 * `ChatUI.supportedReactions` allows to define own set of supported message reaction
 * `ChatUI.style` allows to override global style of UI components, like the TextStyle.
