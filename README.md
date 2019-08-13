# stream-chat-android

[![Build Status](https://travis-ci.com/GetStream/stream-chat-android.svg?branch=master)](https://travis-ci.com/GetStream/stream-chat-android) ![version](https://jitpack.io/v/GetStream/stream-chat-android.svg) [![Component Reference](https://img.shields.io/badge/docs-component%20reference-blue.svg)](https://getstream.github.io/stream-chat-android/)

[stream-chat-android](https://github.com/GetStream/stream-chat-android) is the official Android SDK for [Stream Chat](https://getstream.io/chat), a service for building chat applications.

You can sign up for a Stream account at [https://getstream.io/chat/get_started/](https://getstream.io/chat/get_started/).
This library includes both a low level chat SDK and a set of reusable UI components.
Most users start out with the UI components, and fall back to the lower level API when they want to customize things.




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

## UI Components

### ChannelList

The ChannelListView shows a list of channel previews.
Typically it will show an unread/read state, the last message and who is participating in the conversation.

The easiest way to render a ChannelList is to add it to your layout:

```
<com.getstream.sdk.chat.view.ChannelListView android:id="@+id/channelList"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_marginBottom="10dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent"
     />
```

And in activity do something like this:

```
package io.getstream.chat.example;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.ChannelListItemAdapter;
import com.getstream.sdk.chat.adapter.ChannelListItemViewHolder;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

import java.util.HashMap;

import io.getstream.chat.example.databinding.ActivityMainBinding;

import static com.getstream.sdk.chat.enums.Filters.in;


/**
 * This activity shows a list of channels
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";
    final String USER_ID = "broken-waterfall-5";
    // User token is typically provided by your server when the user authenticates
    final String USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJva2VuLXdhdGVyZmFsbC01In0.d1xKTlD_D0G-VsBoDBNbaLjO-2XWNA8rlTm4ru4sMHg";
    private ChannelListViewModel viewModel;

    // establish a websocket connection to stream
    protected Client configureStreamClient() {
        Client client = StreamChat.getInstance(this.getApplication());
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Broken waterfall");
        extraData.put("image", "https://bit.ly/2u9Vc0r");
        User user = new User(USER_ID, extraData);
        client.setUser(user, USER_TOKEN);

        return client;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup the client
        Client client = configureStreamClient();

        // we're using data binding in this example
        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Specify the current activity as the lifecycle owner.
        binding.setLifecycleOwner(this);

        // most the business logic for chat is handled in the ChannelListViewModel view model
        viewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
        // set the viewModel data for the activity_main.xml layout
        binding.setViewModel(viewModel);

        ChannelListItemAdapter adapter = new ChannelListItemAdapter(this);
        adapter.setCustomViewHolder(ChannelListItemViewHolder.class);
        binding.channelList.setViewModel(viewModel, this, adapter);

        // query all channels where the current user is a member
        // FilterObject filter = in("members", USER_ID);
        FilterObject filter = in("type", "messaging");
        viewModel.setChannelFilter(filter);

        // setup an onclick listener to capture clicks to the user profile or channel
        MainActivity parent = this;
        binding.channelList.setOnChannelClickListener(channel -> {
            // open the channel activity
            Intent intent = new Intent(parent, ChannelActivity.class);
            intent.putExtra(EXTRA_CHANNEL_TYPE, channel.getType());
            intent.putExtra(EXTRA_CHANNEL_ID, channel.getId());
            startActivity(intent);
        });
        binding.channelList.setOnUserClickListener(user -> {
            // TODO: open your user profile
        });

    }
}
```

#### Listeners

The following listeners can be set

* setOnChannelClickListener
* setOnLongClickListener
* setOnUserClickListener

#### Styling using Attributes

The following attributes are available:

```
<com.getstream.sdk.chat.view.ChannelListView android:id="@+id/channelList"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_marginBottom="10dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent"
    stream:avatarWidth="40dp"
    stream:avatarHeight="40dp"
    stream:avatarInitialsTextSize="16sp"
    stream:avatarInitialsTextColor="#000"
    stream:avatarInitialsTextStyle="bold"
    stream:dateTextColor="#858585"
    stream:dateTextSize="11sp"
    stream:titleTextColor="#000"
    stream:titleTextSize="16sp"
    stream:titleTextStyleChannel="bold"
    stream:unreadTitleTextColor="#000"
    stream:unreadTitleTextStyle="bold"
    stream:messageTextColor="#858585"
    stream:messageTextSize="16sp"
    stream:messageTextStyle="normal"
    stream:unreadMessageTextColor="#000"
    stream:unreadMessageTextStyle="bold"
    stream:channelPreviewLayout="@layout/list_item_channel_custom"
     />
```

#### Changing the layout

If you need to make a bigger change you can swap the layout for the channel previews.

```
<com.getstream.sdk.chat.view.ChannelListView android:id="@+id/channelList"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_marginBottom="10dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent"
    stream:channelPreviewLayout="@layout/list_item_channel_custom"
     />
```

That only works for simple changes where you don't change the IDs of views, or their types.
You can find the default layout and copy and paste it in **list_item_channel.xml**

#### Custom Viewholder

If you need full control over the styling for the channel preview you can overwrite the view holder.

```
ChannelListItemAdapter adapter = new ChannelListItemAdapter(this);
adapter.setCustomViewHolder(MyCustomViewHolder.class);
binding.channelList.setViewModel(viewModel, this, adapter);
```

You'll typically want to extend either the `ChannelListItemViewHolder` or the `BaseChannelListItemViewHolder` class.

#### Client usage

Alternatively you can of course build your own ChannelListView using the low level client.

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
