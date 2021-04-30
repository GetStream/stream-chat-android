---
id: uiChannelListHandlingChannelActions
title: Handling Channel Actions
sidebar_position: 3
---
`ChannelListHeaderView` and `ChannelListView` comes with a set of channel actions out of the box. Actions on `ChannelListView` items are available on swipe. You can:
* See current user avatar
* See current user online status
* See channel members
* Delete the channel if you have sufficient permissions
* Leave the channel if it's a group channel

| Light Mode | Dark Mode |
| --- | --- |
|![light_mode](https://user-images.githubusercontent.com/17440581/108343344-1ee4ff80-71dc-11eb-9914-6dac8670f4d1.png)|![dark_mode](https://user-images.githubusercontent.com/17440581/108343348-20162c80-71dc-11eb-9dc5-43fff7c860c9.png)|

There are some actions (e.g. clicking on the current user avatar, member, channel, or _Viewing Info_) that require additional handling:
```kotlin
channelListHeaderView.setOnActionButtonClickListener {
    // Handle Action Button Click
}
channelListHeaderView.setOnUserAvatarClickListener {
    // Handle User Avatar Click
}
channelListView.setChannelItemClickListener { channel ->
    // Handle Channel Click
}
channelListView.setChannelInfoClickListener { channel ->
    // Handle Channel Info Click
}
channelListView.setUserClickListener { user ->
    // Handle Member Click
}
```
The full list of available listeners is available [here (ChannelListHeaderView)](https://getstream.github.io/stream-chat-android/stream-chat-android-ui-components/stream-chat-android-ui-components/io.getstream.chat.android.ui.channel.list.header/-channel-list-header-view/index.html) and [here (ChannelListView)](https://getstream.github.io/stream-chat-android/stream-chat-android-ui-components/stream-chat-android-ui-components/io.getstream.chat.android.ui.channel.list/-channel-list-view/index.html).
