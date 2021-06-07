# Channel List Header

<!-- TODO: Import whatever makes sense to import from https://getstream.io/chat/docs/android/channel_list_header_view/?language=kotlin -->

## Overview

<!-- TODO: Brief description and a couple screenshots with default styling. -->

## Handling Actions

<!-- TODO: This section -->

There are some actions that require additional handling:

```kotlin
channelListHeaderView.setOnActionButtonClickListener {
    // Handle Action Button Click
}
channelListHeaderView.setOnUserAvatarClickListener {
    // Handle User Avatar Click
}
```

## Customizations

### Customization with XML Attributes

<!-- TODO: Customization description -->

5. Modify `ChannelListHeaderView`'s attributes:
```xml
    <io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
        android:id="@+id/channelListHeaderView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:streamUiShowActionButton="false"
        />
```
