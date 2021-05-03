---
id: uiSampleChatCustomizationsEmptyStateView
title: Changing Components (empty state view)

sidebar_position: 1
---
Just like other components, those three views come together with view models which are responsible for providing all necessary data for its views. However, unlike the other view models, they require some additional setup to pass information between different chat components:
```kotlin
// Create view models
val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId")
val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
val messageListViewModel: MessageListViewModel by viewModels { factory }
val messageInputViewModel: MessageInputViewModel by viewModels { factory }

// Bind view models
messageListHeaderViewModel.bindView(messageListHeaderView, viewLifecycleOwner)
messageListViewModel.bindView(messageListView, viewLifecycleOwner)
messageInputViewModel.bindView(messageInputView, viewLifecycleOwner)

// Let both message list header and message input know when we open a thread
messageListViewModel.mode.observe(this) { mode ->
    when (mode) {
        is MessageListViewModel.Mode.Thread -> {
            messageListHeaderViewModel.setActiveThread(mode.parentMessage)
            messageInputViewModel.setActiveThread(mode.parentMessage)
        }
        MessageListViewModel.Mode.Normal -> {
            messageListHeaderViewModel.resetThread()
            messageInputViewModel.resetThread()
        }
    }
}

// Let the message input know when we are editing a message
messageListView.setMessageEditHandler { message ->
    messageInputViewModel.editMessage.postValue(message)
}

// Handle navigate up state
messageListViewModel.state.observe(this) { state ->
    if (state is MessageListViewModel.State.NavigateUp) {
        // Handle navigate up
    }
}

// Handle back button behaviour correctly when you're in a thread
val backHandler = {
    messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
}
messageListHeaderView.setBackButtonClickListener(backHandler)
// You should also consider overriding default Activity's back button behaviour
```
At that point you should be able to display the default chat view:

| Light Mode | Dark Mode |
| --- | --- |
|![chat view light](/img/chat_view_light.png)|![chat view dark](/img/chat_view_dark.png)|

From that point, you will be able to display and send messages, conduct different actions, as well as view different channel info.
