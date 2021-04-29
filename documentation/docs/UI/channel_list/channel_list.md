---
id: uiChannelListCreatingLayout
title: Creating Layout
sidebar_position: 1
---
The SDK provides two views: `ChannelListHeaderView` and `ChannelListView` which work best together to display the channel and other useful information.

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >

    <io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
        android:id="@+id/channelListHeaderView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        />

    <io.getstream.chat.android.ui.channel.list.ChannelListView
        android:id="@+id/channelListView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/channelListHeaderView"
        />

</androidx.constraintlayout.widget.ConstraintLayout>
```
