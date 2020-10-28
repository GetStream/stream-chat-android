# Official Android SDK for [Stream Chat](https://getstream.io/chat/)

[![Build Status](https://travis-ci.com/GetStream/stream-chat-android.svg?branch=master)](https://travis-ci.com/GetStream/stream-chat-android) ![version](https://jitpack.io/v/GetStream/stream-chat-android.svg) [![Component Reference](https://img.shields.io/badge/docs-component%20reference-blue.svg)](https://getstream.github.io/stream-chat-android/)

<p align="center">
  <a href="https://getstream.io/tutorials/ios-chat/"><img src="https://user-images.githubusercontent.com/88735/70033290-6e2b1a00-15af-11ea-8f6d-18caaae68ae4.png" width="60%" /></a>
</p>

## 4.x

4.x is based on Kotlin and splits out the client, offline support and UX components. It adds seamless offline support, improved performance, makes it easier to integrate and has better test coverage. 

[stream-chat-android](https://github.com/GetStream/stream-chat-android) is the official Android SDK for [Stream Chat](https://getstream.io/chat), a service for building chat and messaging applications. This library includes both a low-level chat SDK and a set of reusable UI components. Most users start with the UI components, and fall back to the lower level API when they want to customize things.

<img align="right" src="https://getstream.imgix.net/images/chat-android/android_chat_art@1x.png?auto=format,enhance" width="50%" />

**Quick Links**

* [Register](https://getstream.io/chat/trial/) to get an API key for Stream Chat
* [Java Chat Tutorial](https://getstream.io/tutorials/android-chat/#java)
* [Kotlin Chat Tutorial](https://getstream.io/tutorials/android-chat/#kotlin)
* [Java API Docs](https://getstream.io/chat/docs/java/#introduction)
* [Kotlin API Docs](https://getstream.io/chat/docs/kotlin/#introduction)
* [Chat UI Kit](https://getstream.io/chat/ui-kit/)
* [WhatsApp clone Tutorial](https://getstream.io/blog/build-whatsapp-clone/)

## Java/Kotlin Chat Tutorial

The best place to start is the [Android Chat Tutorial](https://getstream.io/tutorials/android-chat/#java). It teaches you how to use this SDK and also shows how to make frequently required changes. You can use either [Java](https://getstream.io/tutorials/android-chat/#java) or [Kotlin](https://getstream.io/tutorials/android-chat/#kotlin) depending on your preference.

## Clone the Github Example App

This repo includes a fully functional example app. To run the example app:

```shell
git clone git@github.com:GetStream/stream-chat-android.git
```

Open the project in Android Studio and set up your emulator (we're using Pixel 3, API 29 at the moment). Note that the Gradle sync process can take some time when you first open the project. 

## Docs

This library provides:

- A low-level client for making API calls and receiving chat events
- `ViewModel` for list of channels and `ViewModel` for channel
- Four reusable chat views:
    - [Channel List](https://getstream.io/chat/docs/channel_list_view/?language=kotlin)
    - [Message List](https://getstream.io/chat/docs/message_list_view/?language=kotlin)
    - [Message Input](https://getstream.io/chat/docs/message_input_view/?language=kotlin)
    - [Channel Header](https://getstream.io/chat/docs/channel_header_view/?language=kotlin)

The documentation for LiveData and the custom views is available here:
[https://getstream.io/chat/docs/android_overview/?language=kotlin](https://getstream.io/chat/docs/android_overview/?language=kotlin)

### Chat API

The low-level Chat API docs are available for both [Kotlin](https://getstream.io/chat/docs/kotlin/) and [Java](https://getstream.io/chat/docs/java/).

## Supported features

- Channels list UI
- Channel UI
- Message Reactions
- Link preview
- Images, Videos and Files attachments
- Edit and Delete message
- Typing Indicators
- Read Indicators
- Push Notifications
- Image gallery
- GIF support
- Light/Dark themes
- Style customization
- UI customization
- Threads
- Slash commands
- Markdown messages formatting


## Installing the Java Chat SDK

- **Step 1** Add repository into root build.gradle

```gradle
allprojects {
    repositories {
    ...
    maven {
        url 'https://jitpack.io' }
    }
}
```

- **Step 2** Add library dependency into app build.gradle

> See the jitpack badge above for the latest version number

```gradle
android {
    ...
    compileOptions {
        
        //for java projects
        implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"

        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.github.GetStream:stream-chat-android:version'
}
```

## Proguard/R8

If you're using Proguard/R8, you'll want to have a look at the [proguard file we use for the sample](https://github.com/GetStream/stream-chat-android/blob/master/sample/proguard-rules.pro).

## Setup Stream Chat

Make sure to initialize the SDK only once; the best place to do this is in your `Application` class.

```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Chat chat = new Chat.Builder("api-key", this).build();
    }
}
```

With this, you will be able to retrieve Chat instance from any part of your application using `Chat.instance()`. Here's an example:

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Chat chat = Chat.instance();
        ...
    }
```

## Online status

Connection status to Chat is available via `Chat.instance().getOnlineStatus()` which returns a LiveData object you can attach observers to.

```java
Chat.instance().getOnlineStatus().observe(...);
```

## Markdown support

Markdown support is based on [Markwon: 4.1.2](https://github.com/noties/Markwon).
Currently SDK doesn't support all `Markwon` features and limited to this plugins:
- [CorePlugin](https://noties.io/Markwon/docs/v4/core/core-plugin.html)
- [LinkifyPlugin](https://noties.io/Markwon/docs/v4/linkify/)
- [ImagesPlugin](https://noties.io/Markwon/docs/v4/image/)
- [StrikethroughPlugin](https://noties.io/Markwon/docs/v4/ext-strikethrough/)

If you want to use another library for `Markdown` or extend the `Markwon` plugins you can use the code below

```java
Chat chat = new Chat.Builder(apiKey, this)
    .markdown((textView, text) -> {
        textView.setText(text)
    })
    .build();
```

## Debug and development

### Logging
By default, logging is disabled. You enable logs and set log level when initializing `Chat`:

```java
Chat chat = new Chat.Builder("api-key", context).logLevel(ChatLogLevel.ALL).build()
```

If you need to intercept logs you can pass logger handler:

```java
Chat chat = new Chat.Builder("api-key", context)
    .loggerHandler(new ChatLoggerHandler() {
        @Override
        public void logI(@NotNull Object tag, @NotNull String message) {

        }  
    }
    .build()
```

### Editing this library

This guide assumes that you're working on your own project in the `project` folder and clone the chat SDK library in a separate folder.

1. First of all, you'll want to clone this repo

```shell
git clone git@github.com:GetStream/stream-chat-android.git
```

2. Next you'll edit your project's `settings.gradle` and add this

```gradle
include ':chat'

project(":chat").projectDir = new File("ABSOLUTEPATHTOstream-chat-android here")
```

3. Open up your `project/app/build.gradle` and replace the production SDK with your local copy

```gradle
//implementation 'com.github.getstream:stream-chat-android:version'
implementation project(':chat')
```

4. Next open up `project/build.gradle`. 

Add the following to the buildscript {} entry.

```gradle
buildscript {
...

    ext {
        googleServiceVersion = '4.3.2'
        gradleVersion = '3.5.2'
        gradlePluginVersion = '2.1'
        jacocoVersion = '0.1.4'
        mannodermausVersion = '1.5.1.0'

    }
    
...
}
```

Next, set up these libraries in the dependencies. (They are needed to compile stream-chat-android)

```gradle
buildscript {
    dependecies {
    ....
    
    classpath "com.android.tools.build:gradle:$gradleVersion"
        classpath "com.github.dcendents:android-maven-gradle-plugin:$gradlePluginVersion"
        classpath "com.google.gms:google-services:$googleServiceVersion"
        classpath "de.mannodermaus.gradle.plugins:android-junit5:$mannodermausVersion"
        classpath "com.dicedmelon.gradle:jacoco-android:$jacocoVersion"
    }
}
```

5.

Hit build/clean project in android studio and build your app.


## FAQ

### Channel List loading icons spins forever

Not setting the lifecycle owner on a data binding can cause the channel list loading icon to spin forever.

```java
mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false);
mBinding.setLifecycleOwner(this);
```

It's also possible that the permissions for your app are cached. Try uninstalling the app from your emulator and reinstalling to ensure you have the permissions required by this library.

### Images are not loaded
In most cases you can try to see the reason in logcat with tag `Glide`. One of the reasons is that app tries to load image from http url, but not https. To fix it you need to define [network security config](https://developer.android.com/training/articles/security-config).

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Keep in mind this example allows to make any http request, to define proper security config read Android documentation-->
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

And update your `Manifest`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"/>
```

### Localize the UI with Translations

You can translate all [strings](https://github.com/GetStream/stream-chat-android/blob/master/library/src/main/res/values/strings.xml) of SDK by overriding string keys.<br/>
The example app has a few examples:
- [German](https://github.com/GetStream/stream-chat-android/blob/master/sample/src/main/res/values-de/strings.xml)
- [Spanish](https://github.com/GetStream/stream-chat-android/blob/master/sample/src/main/res/values-es/strings.xml)
- ...
