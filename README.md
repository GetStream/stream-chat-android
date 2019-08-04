# stream-chat-android

[![Build Status](https://travis-ci.com/GetStream/stream-chat-android.svg?branch=master)](https://travis-ci.com/GetStream/stream-chat-android) ![version](https://jitpack.io/v/GetStream/stream-chat-android.svg) [![Component Reference](https://img.shields.io/badge/docs-component%20reference-blue.svg)](https://getstream.github.io/stream-chat-android/)

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

- **Initialize Client**

 Java
~~~java
StreamChat streamChat = new StreamChat("API KEY");
~~~

 Kotlin
~~~
val streamChat = StreamChat(API_KEY)
~~~

- **Initialize User**

 Java
~~~java
User user = new User("USER ID", EXTRA_DATA); // EXTRA_DATA : HashMap
~~~

 Kotlin
~~~
val user = User(USER_ID, extraData)
~~~

- **Set User**

 Java
~~~java
streamChat.setUser(user, "USER TOKEN");
~~~

 Kotlin
~~~
streamChat.setUser(user, "USER TOKEN")
~~~

- **Adding Single Conversation Screen**

Adding chat is simple as the library comes with a built-in **ChatActivity** of library class which loads messages for a specified activeChannel using the APIs and renders its content.

  1. Set specified **activeChannel** object with activeChannel Id, activeChannel name and activeChannel image.
  2. Set  specified **activeChannel** to **streamChat**.
  3. Navigate to **ChatActivity** 

 Java
~~~java
// Setting Channel
Channel activeChannel = new Channel("CHANNEL TYPE", "CHANNEL ID", EXTRA_DATA); // EXTRA_DATA : HashMap
streamChat.setChannel(activeChannel);
 
// Start ChatActivity
Intent i = new Intent(this, ChatActivity.class);
startActivity(i);
~~~

 Kotlin 
~~~kotlin
// Setting Channel
val activeChannel = Channel("CHANNEL TYPE", "CHANNEL ID", EXTRA_DATA) // EXTRA_DATA : HashMap
streamChat.activeChannel = activeChannel

// Start ChatActivity
val i = Intent(this, ChatActivity::class.java)
startActivity(i)
~~~

- **Multiple conversations**

We can add **ChannelListFragment** of library in any Activity or Fragment directly.

 Xml layout

~~~xml
<FrameLayout
    android:fitsSystemWindows="true"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />	
~~~

 Java
~~~java
ChannelListFragment fragment = new ChannelListFragment(); 
fragment.containerResId = R.id.container;  
FragmentManager fragmentManager = getSupportFragmentManager();
FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
fragmentTransaction.replace(R.id.container, fragment);
fragmentTransaction.addToBackStack(null);
fragmentTransaction.commit();
~~~

 Kotlin
~~~kotlin
val fragment = ChannelListFragment()
fragment.containerResId = R.id.container  
val fragmentManager = supportFragmentManager
val fragmentTransaction = fragmentManager.beginTransaction()
fragmentTransaction.replace(R.id.container, fragment)
fragmentTransaction.addToBackStack(null)
fragmentTransaction.commit()
~~~

## Customize

You can customize UI components by overriding resource values.

Following resource values can be overridden. 

### colors
**Theme**
- "chat_theme"
- "channel_theme"
 
**Input Message Box**
- "input_message_box_stroke_select"
- "input_message_box_background"
 
**Message Item View**
- "message_background_incoming"
- "message_background_outgoing"
- "message_text_incoming"
- "message_text_outgoing"
- "mesage_border"
- "user_intials_background"
 
**Message Reaction View**
- "reaction_background"

### dimensions
**Message Item View**
- "message_text_font_size"

### styles
**Channel preview**
- "channel_preview_initials"
- "channel_preview_avatar"
- "channel_preview_channel_name"
- "channel_preview_channel_last_message"
- "channel_preview_channel_last_message_date"
- "channel_preview_channel_unread_indicator"

***Note**: If you don’t want to override it, you should not use the same values in resource!*


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


TODO: https://getstream.io/chat/docs/#introduction but with Android code examples
