# Message List Header


`MessageListHeaderView` is a component that supposed to be used on a single-channel screen and shows the channel's name and avatar, members and online members count, current connection status, and back button.

 | Light Mode | Dark Mode |
 | --- | --- |
 |![Light_mode](../../assets/message_list_header.png)|![Dark_mode](../../assets/message_list_header_dark.png)|

## Usage

To use `MessageListHeaderView`, include it in your XML layout as shown below:
```XML
<io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
    android:id="@+id/messageListHeaderView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    />
```

We recommend using the `MessageListHeaderViewModel` that gets all needed data from the Stream API and then renders it in the view.

The basic setup of the ViewModel and connecting it to the view is done the following way:
 ```kotlin
// 1. Init view model
val viewModel: MessageListHeaderViewModel by viewModels {
    MessageListViewModelFactory(cid = "messaging:123")
}

// 2. Bind view and viewModel
viewModel.bindView(messageListHeaderView, lifecycleOwner)
```
By default, the ViewModel will make the view display useful channel information and call the view to display "Searching for network" state when needed.

 | Light Mode | Dark Mode |
 | --- | --- |
 |![Light_mode](../../assets/message_list_header_waiting_for_network.png)|![Dark_mode](../../assets/message_list_header_waiting_for_network_dark.png)|

## Handling Actions

By default, `MessageListHeaderView` displays all the views described in previous sections but none of them comes with a default click behavior. You can change that by setting the following listeners:
```kotlin
messageListHeaderView.setBackButtonClickListener {
    // Handle Back Button Click
}
messageListHeaderView.setAvatarClickListener {
    // Handle Avatar Click
}
messageListHeaderView.setTitleClickListener {
    // Handle Title Click
}
messageListHeaderView.setSubtitleClickListener {
    // Handle Subtitle Click
}
```

## Customizations

### Customization with XML Attributes

The appearance of `MessageListHeaderView` can be conveniently modified using its XML attributes.
```xml
<io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
    android:id="@+id/messageListHeaderView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:streamUiMessageListHeaderShowBackButton="false"
    app:streamUiMessageListHeaderTitleTextColor="#FF0000"
    app:streamUiMessageListHeaderDefaultLabelTextStyle="bold"
    />
```

The example above hides the back button, makes the title text red and subtitle text bold.

| Before | After |
| --- | --- |
|![Light_mode](../../assets/message_list_header.png)|![Dark_mode](../../assets/message_list_header_customization.png)|

A full list of available XML attributes is available [here](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/attrs_message_list_header_view.xml)
