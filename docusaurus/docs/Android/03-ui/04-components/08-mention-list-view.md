# Mention List View

`MentionListView` is a UI Component that shows previews of messages that contain mentions of the current user.

| Light Mode | Dark Mode |
| --- | --- |
|![Light mode](../../assets/mentions_list_view_light.png)|![Dark mode](../../assets/mentions_list_view_dark.png)|

## Usage

You can add this View via XML:

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

We recommend using this view with its [ViewModel](../01-overview.md#viewmodels), which supplies it with data from the Stream API.

The basic setup of the ViewModel and connecting it to the View is done the following way:

```kotlin
val viewModel: MentionListViewModel by viewModels()
viewModel.bindView(binding.mentionsListView, viewLifecycleOwner)
```

From that point, you should be able to see messages which contain mentions of the current user.

:::note
`bindView` sets listeners on the View and the ViewModel. Any additional listeners should be set _after_ calling `bindView`.
:::

## Handling Actions

`MentionListView` allows you to configure certain actions on it:

```kotlin
mentionListView.setMentionSelectedListener { message ->
    // Handle a mention item being clicked
}
```

The full list of available listeners is available [here](https://getstream.github.io/stream-chat-android/stream-chat-android-ui-components/stream-chat-android-ui-components/io.getstream.chat.android.ui.mention.list/-mention-list-view/index.html).
