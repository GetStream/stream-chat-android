---
id: uiMentionListView
title: Mention List View
sidebar_position: 4
---

## Creating layout
`MentionListView` is a view that is responsible for showing previews of messages which contains current user mention.

You can declare the `MentionListView` inside a layout file:

```
<io.getstream.chat.android.ui.mention.list.MentionListView
        android:id="@+id/mentionsListView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        />
```

| Light Mode | Dark Mode |
| --- | --- |
|![light](https://user-images.githubusercontent.com/17440581/108487097-26b9a800-729f-11eb-9575-484c6f651102.png)|![dark](https://user-images.githubusercontent.com/17440581/108487094-25887b00-729f-11eb-9726-348ee1342242.png)|

## Adding Mention List View And Binding With View Model
Here's an example layout containing `MentionListView`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >

    <io.getstream.chat.android.ui.mention.list.MentionListView
        android:id="@+id/mentionsListView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        />

</androidx.constraintlayout.widget.ConstraintLayout>
```
Just like other views - `MentionListView` comes together with a view model which is responsible for providing necessary data:
```kotlin
// Create view model
val viewModel: MentionListViewModel by viewModels()
// Bind with view
viewModel.bindView(mentionListView, viewLifecycleOwner)
```
From that point, you should be able to see messages which contain current user mention.

## Handling Mention List View Actions
`MentionListView` allows to configure common actions (e.g. click on a single item):
```kotlin
mentionListView.setMentionSelectedListener { message ->
    // Handle mention click
}
```
The full list of available listeners is available [here](https://getstream.github.io/stream-chat-android/stream-chat-android-ui-components/stream-chat-android-ui-components/io.getstream.chat.android.ui.mention.list/-mention-list-view/index.html).
