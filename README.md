# stream-chat-android

[![Build Status](https://travis-ci.com/GetStream/stream-chat-android.svg?branch=master)](https://travis-ci.com/GetStream/stream-chat-android) ![version](https://jitpack.io/v/GetStream/stream-chat-android.svg)

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

~~~java
StreamChat streamChat = new StreamChat("API KEY");
~~~

- **Initialize User**

~~~java
User user = new User("USER ID", "USER NAME", "USER IMAGE");
~~~

- **Set User**

~~~java
String userToken = streamChat.createUserToken("API SECRET", "USER ID");
streamChat.setUser(user, userToken);
~~~

- **Adding Single Conversation Screen**

Adding chat is simple as the library comes with a built-in **ChatActivity** of library class which loads messages for a specified channel using the APIs and renders its content.

  1. Set specified **channel** object with channel Id, channel name and channel image.
  2. Set  specified **channel** to **streamChat**.
  3. Navigate to **ChatActivity** 

 Java
~~~java
// Setting Channel
Channel channel = new Channel();
channel.setId(channelId);
channel.setName(<Channel Name>);
channel.setImageURL(<Channel Image>);
streamChat.setChannel(channel);
 
// Start ChatActivity
Intent i = new Intent(this, ChatActivity.class);
startActivity(i);
~~~

 Kotlin 
~~~kotlin
// Setting Channel
val channel = Channel()
channel.id = channelId
channel.name = <Channel Name>
channel.imageURL = <Channel Image>
streamChat.setChannel(channel)
 
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
private void addChannelListFragment(StreamChat streamChat) {
   ChannelListFragment fragment = new ChannelListFragment(); 
   fragment.containerResId = R.id.container;
   fragment.streamChat = streamChat;
   FragmentManager fragmentManager = getSupportFragmentManager();
   FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
   fragmentTransaction.replace(R.id.container, fragment);
   fragmentTransaction.addToBackStack(null);
   fragmentTransaction.commit();
}
~~~

 Kotlin
~~~kotlin
fun addChatListFragment(streamChat: StreamChat) {
   val fragment = ChannelListFragment()
   fragment.containerResId = R.id.container
   fragment.streamChat = streamChat
   val fragmentManager = supportFragmentManager
   val fragmentTransaction = fragmentManager.beginTransaction()
   fragmentTransaction.replace(R.id.container, fragment)
   fragmentTransaction.addToBackStack(null)
   fragmentTransaction.commit()
}
~~~

## Customize

You can customize UI components by overriding resource values.

Following resource values can be overridden. 

- **colors**

Theme
- "chat_theme"
- "channel_theme"
 
Input Message Box
- "input_message_box_stroke_select"
- "input_message_box_background"
 
Message Item View
- "message_background_incoming"
- "message_background_outgoing"
- "message_text_incoming"
- "message_text_outgoing"
- "mesage_border"
- "user_intials_background"
 
Message Reaction View
- "reaction_background"

- **dimensions**

Message Item View
- "message_text_font_size"

- **styles **

Channel preview
- "channel_preview_initials"
- "channel_preview_avatar"
- "channel_preview_channel_name"
- "channel_preview_channel_last_message"
- "channel_preview_channel_last_message_date"

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
