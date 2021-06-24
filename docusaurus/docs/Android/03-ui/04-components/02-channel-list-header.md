# Channel List Header


`ChannelListHeaderView` is a component designed to be displayed on the channels screen of your app. 
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
The basic setup of the view model and connecting it with the view can be done in the following way:
 ```kotlin
// Instantiate the view model 
val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()
// Bind the view model with ChannelListView 
channelListHeaderViewModel.bindView(chanelHeaderView, viewLifecycleOwner)
// Optional: setup listeners for user avatar and action button 
chanelHeaderView.setOnActionButtonClickListener {
    navigateSafely(R.id.action_homeFragment_to_addChannelFragment)
}
chanelHeaderView.setOnUserAvatarClickListener {
    binding.drawerLayout.openDrawer(GravityCompat.START)
}
```
All the logic of subscribing to data emitted by view model is provided by the `ChannelListHeaderViewModel::bindView` function. 
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

The example above, hides the avatar view, makes the title text bold and sets the drawable of the action button to a drawable specified.
    
| Before | After |
| --- | --- |
|![Light_mode](../../assets/channels_header.png)|![Dark_mode](../../assets/channels_header_after_customization.png)|

A full list of available XML attributes is available [here](https://github.com/GetStream/stream-chat-android/blob/develop/stream-chat-android-ui-components/src/main/res/values/attrs_channel_list_header_view.xml)
