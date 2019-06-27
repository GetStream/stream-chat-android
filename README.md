# stream-chat-android

[![Build Status](https://travis-ci.com/GetStream/stream-chat-android.svg?branch=master)](https://travis-ci.com/GetStream/stream-chat-android)

[stream-chat-android](https://github.com/GetStream/stream-chat-android) is the official Android SDK for [Stream Chat](https://getstream.io/chat), a service for building chat applications.

You can sign up for a Stream account at [https://getstream.io/chat/get_started/](https://getstream.io/chat/get_started/).

## Installation

- **Step 1** Add repository into root build.gradle

~~~gradle
allprojects {
    repositories {
    ...
    maven {
        url 'https://jitpack.io' }
    }
}
~~~

- **Step 2** Add library dependency into app build.gradle

> See the jitpack badge above for the latest version number

~~~gradle
android {
    ...
    dataBinding {
        enabled = true
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.github.getstream:stream-chat-android:<latest-version>'
}
~~~

## Getting started

- **Initialize client**

~~~java
StreamChat streamChat = new StreamChat("API KEY", "FIREBASE SERVER KEY");
~~~

- **Initialize User**

~~~java
User user = new User("USER ID", "USER NAME", "USER IMAGE");
~~~

- **Set User**

~~~java
String userToken = streamChat.creatUserToken("API SECRET", "USER ID");
streamChat.setUser(user, userToken);
~~~

- **Adding GetStream Chat Fragment**

You can add getstream chat fragment in any Activity or Fragment

~~~xml
<FrameLayout
	android:fitsSystemWindows="true"
	android:id="@+id/title_fragment"
	android:layout_width="match_parent"
	android:layout_height="match_parent" />	
~~~

## Documentation

TODO - Java Chat Tutorial page
[Official API Docs](https://getstream.io/chat/docs)

## Supported features

- A group chat
- Channel list
- Reactions
- A link preview
- Attach images, videos or files
- Commands (e.g. `/giphy`)
- Edit a message
- Typing events
- Read events
- Threads
- Notifications
- Opening a link in the internal browser
- Image gallery
- Supporting Gifs
- Light/Dark styles
- Style customization
- UI customization

## Getting started


TODO: https://getstream.io/chat/docs/#introduction but with Swift code examples
