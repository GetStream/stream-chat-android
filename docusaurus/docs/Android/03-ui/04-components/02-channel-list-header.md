# Channel List Header


`ChannelListHeaderView` is a component that shows the title of the channels list, current connection status, avatar of the current user, 
and provides an action button which can be used to create a new conversation. 
It is designed to be displayed at the top of the channels screen of your app. 
It consists of the following elements: _User avatar_, _Title view_, _Action button_.
 
 | Light Mode | Dark Mode |
 | --- | --- |
 |![Light_mode](../../assets/channels_header.png)|![Dark_mode](../../assets/channels_header_dark.png)|
 
## Usage

To use `ChannelListHeaderView` in your layout, include it in your XML layout as shown below:
```XML
<io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
    android:id="@+id/channelListHeaderView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    />
```

`ChannelListHeaderView` is supposed to work with `ChannelListHeaderViewModel`. 
The basic setup of the ViewModel and connecting it with the view can be done in the following way:
 ```kotlin
// Instantiate the ViewModel
val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()
// Bind the ViewModel with ChannelListView 
channelListHeaderViewModel.bindView(chanelHeaderView, viewLifecycleOwner)
// Optional: setup listeners for user avatar and action button 
chanelHeaderView.setOnActionButtonClickListener {
    // e.g. Navigate to "Add new channel screen"
}
chanelHeaderView.setOnUserAvatarClickListener {
    // handle avatar click here
}
```
All the logic of subscribing to data emitted by the ViewModel is provided by the `ChannelListHeaderViewModel::bindView` function. 
By default, the ViewModel will make the view display avatar of the currently logged-in user and call the view to display "Searching for network" state when needed.
  
 | Light Mode | Dark Mode |
 | --- | --- |
 |![Light_mode](../../assets/channels_header_waiting_for_network.png)|![Dark_mode](../../assets/channels_header_waiting_for_network_dark.png)|

## Handling Actions

By default the view displays avatar and action button. In order to use them you need to set the following listeners:
```kotlin
channelListHeaderView.setOnActionButtonClickListener {
    // Handle Action Button Click
}
channelListHeaderView.setOnUserAvatarClickListener {
    // Handle User Avatar Click
}
```
If you want to hide those views you need to add XML attributes that modify their visibility. This is explained in the next section.

## Customizations

### Customization with XML Attributes

The appearance of `ChannelListHeaderView` can be conveniently modified using its XML attributes.   
```xml
    <io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
        android:id="@+id/channelListHeaderView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:streamUiShowUserAvatar="false"
        app:streamUiShowOfflineProgressBar="false"
        app:streamUiActionButtonIcon="@drawable/ic_stream_logo"
        app:streamUiOnlineTitleTextStyle="bold"
        />
```

The example above hides the avatar view, makes the title text bold and sets the drawable of the action button to a drawable specified.
    
| Before | After |
| --- | --- |
|![Light_mode](../../assets/channels_header.png)|![Dark_mode](../../assets/channels_header_after_customization.png)|

A full list of available XML attributes is available [here](https://github.com/GetStream/stream-chat-android/blob/develop/stream-chat-android-ui-components/src/main/res/values/attrs_channel_list_header_view.xml)
