# Official Android SDK for [Stream Chat](https://getstream.io/chat/)

![Build status](https://github.com/GetStream/stream-chat-android/workflows/Build%20and%20test/badge.svg)

<p align="center">
  <a href="https://getstream.io/tutorials/android-chat/">
    <img src="/docs/android-chat-messaging-banner.png" width="100%" />
  </a>
</p>

## 4.x

4.x is based on Kotlin and splits out the client, offline support and UX components. It adds seamless offline support, improved performance, makes it easier to integrate, and has better test coverage.

[stream-chat-android](https://github.com/GetStream/stream-chat-android) is the official Android SDK for [Stream Chat](https://getstream.io/chat), a service for building chat and messaging applications. This library includes both a low-level chat SDK and a set of reusable UI components. Most users start with the UI components, and fall back to the lower level API when they want to customize things.

<img align="right" src="https://getstream.imgix.net/images/chat-android/android_chat_art@1x.png?auto=format,enhance" width="50%" />

**Quick Links**

* [Register](https://getstream.io/chat/trial/) to get an API key for Stream Chat
* [Kotlin Chat Tutorial](https://getstream.io/tutorials/android-chat/#kotlin)
* [Java Chat Tutorial](https://getstream.io/tutorials/android-chat/#java)
* [Kotlin API Docs](https://getstream.io/chat/docs/kotlin/#introduction)
* [Java API Docs](https://getstream.io/chat/docs/java/#introduction)
* [Chat UI Kit](https://getstream.io/chat/ui-kit/)
* [WhatsApp clone Tutorial](https://getstream.io/blog/build-whatsapp-clone/)

## Java/Kotlin Chat Tutorial

The best place to start is the [Android Chat Tutorial](https://getstream.io/tutorials/android-chat/#kotlin). It teaches you how to use this SDK and also shows you how to make frequently required changes. You can use either [Java](https://getstream.io/tutorials/android-chat/#java) or [Kotlin](https://getstream.io/tutorials/android-chat/#kotlin) depending on your preference.

## Sample App

This repo includes a fully functional example app featuring threads, reactions, typing indicators, optimistic UI updates and offline storage. To run the sample app, start by cloning this repo:

```shell
git clone git@github.com:GetStream/stream-chat-android.git
```

Next, download Android Studio and open up the `stream-chat-android` folder. You'll want to run the `stream-chat-android-sample` app.
The Gradle sync process can take some time when you first open the project. 

## Docs

The official documentation for the Chat SDK is available [on our website](https://getstream.io/chat/docs/?language=kotlin). Each feature's page shows how to use it with the Android SDK, plus there are further Android-exclusive docs on the [Android Overview page](https://getstream.io/chat/docs/android_overview/?language=kotlin).

The Chat Android SDKs support both Kotlin and Java usage, but *we strongly recommend using Kotlin*. The documentation is available in both languages - see [here](https://getstream.io/chat/docs/?language=java) for the Java version.

This SDK consists of the following modules / artifacts:

- [Chat client](stream-chat-android-client)
- [Offline support and `LiveData` APIs](stream-chat-android-offline)
- [Chat UI/UX](stream-chat-android)

With these modules, the SDK provides:

- A low-level client for making API calls and receiving chat events
- Offline support and LiveData APIs module
- Ready to use ViewModels for displaying a list of channels and a conversation 
- Reusable chat views:
    - [Channel List](https://getstream.io/chat/docs/channels_view/?language=kotlin)
    - [Message List](https://getstream.io/chat/docs/message_list_view/?language=kotlin)
    - [Message Input](https://getstream.io/chat/docs/message_input_view/?language=kotlin)
    - [Channel Header](https://getstream.io/chat/docs/channel_header_view/?language=kotlin)
    - [Message Input View](https://getstream.io/chat/docs/message_input_view/?language=kotlin)

## Supported features

- Channels list UI
- Channel UI
- Message reactions
- Link preview
- Image, video and file attachments
- Editing and deleting messages
- Typing indicators
- Read indicators
- Push notifications
- Image gallery
- GIF support
- Light and dark themes
- Style customization
- UI customization
- Threads
- Slash commands
- Markdown message formatting
- Count for unread messages

## Installing the Kotlin/Java Chat SDK

**Step 1**: Add `mavenCentral` to your repositories in your *project* level `build.gradle` file:

```gradle
allprojects {
    repositories {
        mavenCentral()
    }
}
```

**Step 2**: Add the library as a dependency in your *module* level `build.gradle` file:

> See the [releases page](https://github.com/GetStream/stream-chat-android/releases) for the latest version number.

```gradle
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
    implementation "io.getstream:stream-chat-android:$stream_version"

    // for Java projects
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
}
```

## Setup Stream Chat

Make sure to initialize the SDK only once; the best place to do this is in your `Application` class.

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val apiKey: String = ...
        val user = User().apply {
            id = ...
            image = ...
            name = ...
        }

        val client = ChatClient.Builder(apiKey, this).build()
        val domain = ChatDomain.Builder(client, user, this).offlineEnabled().build()
        val ui = ChatUI.Builder(this).build()
    }
}
```

With this, you will be able to retrieve instances of the different components from any part of your application using `instance()`. Here's an example:

```kotlin
class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val chatClient = ChatClient.instance()
        val chatDomain = ChatDomain.instance()
        val chatUI = ChatUI.instance()
    }
}
```

## Online status

Connection status to Chat is available via `ChatDomain.instance().online` which returns a LiveData object you can attach observers to.

```kotlin
ChatDomain.instance().online.observe(...)
```

## Markdown support

Markdown support is based on [Markwon 4](https://github.com/noties/Markwon). The SDK doesn't support all `Markwon` features, support is limited to these plugins:

- [CorePlugin](https://noties.io/Markwon/docs/v4/core/core-plugin.html)
- [LinkifyPlugin](https://noties.io/Markwon/docs/v4/linkify/)
- [ImagesPlugin](https://noties.io/Markwon/docs/v4/image/)
- [StrikethroughPlugin](https://noties.io/Markwon/docs/v4/ext-strikethrough/)

If you want to use a library other than Markwon or extend the Markwon plugins, you can use the code below to customize Markdown rendering when you build your `ChatUI` instance:

```kotlin
val ui = ChatUI.Builder(context)
    .withMarkdown { textView, text ->
        // do custom rendering here
        textView.text = text
    }
    .build()
```

## Debug and development

### Logging

By default, logging is disabled. You can enable logs and set a log level when initializing `ChatClient`:

```kotlin
val client = ChatClient.Builder(apiKey, context)
    .logLevel(ChatLogLevel.ALL)
    .build()
```

If you need to intercept logs, you can also pass in your own `ChatLoggerHandler`:

```kotlin
val client = ChatClient.Builder(apiKey, context)
    .logLevel(ChatLogLevel.ALL)
    .loggerHandler(object : ChatLoggerHandler {
        override fun logD(tag: Any, message: String) {
            // custom logging
        }

        ...
    })
    .build()
```

To intercept socket errors:

```kotlin
client.subscribeFor<ErrorEvent> { errorEvent: ErrorEvent ->
    println(errorEvent)
}
```

All SDK log tags have the `Chat:` prefix, so you can filter for that those in the logs:

```bash
adb logcat com.your.package | grep "Chat:"
```

Here's a set of useful tags for debugging network communication:

- `Chat:Http`
- `Chat:Events`
- `Chat:SocketService`
