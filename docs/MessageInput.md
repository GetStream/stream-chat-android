### Message Input

Here's an example message input view

```java
<com.getstream.sdk.chat.view.MessageInputView
    android:id="@+id/message_input"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="32dp"
    android:layout_marginBottom="0dp"
    android:background="@color/chat_theme"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    app:layout_constraintStart_toEndOf="@+id/messageList" />
```

#### Styling via attributes

You must use the following properties in your XML to change your MessageInputView.

- **AvatarView**

| Properties                         | Type                   | Default |
| ---------------------------------- | ---------------------- | ------- |
| `app:streamAvatarWidth`            | dimension              | 32dp    |
| `app:streamAvatarHeight`           | dimension              | 32dp    |
| `app:streamAvatarBorderWidth`      | dimension              | 3dp     |
| `app:streamAvatarBorderColor`      | color                  | WHITE   |
| `app:streamAvatarBackGroundColor`  | color                  | #989898 |
| `app:streamAvatarTextSize`         | dimension              | 14sp    |
| `app:streamAvatarTextColor`        | color                  | WHITE   |
| `app:streamAvatarTextStyle`        | normal, bold, italic   | bold    |

- **Attachment Button**

| Properties                                          | Type          | Default    |
| --------------------------------------------------- | ------------- | ---------- |
| `app:streamShowAttachmentButton`                    | boolean       | true       |
| `app:streamAttachmentButtonDefaultIconColor`        | color         | DARK_GRAY  |
| `app:streamAttachmentButtonDefaultIconPressedColor` | color         | WHITE      |
| `app:streamAttachmentButtonDefaultIconDisabledColor`| color         | LIGHT_GRAY |
| `app:streamAttachmentButtonSelectedIconColor`       | color         | BLACK      |
| `app:streamAttachmentButtonIcon`                    | reference     | -          |
| `app:streamAttachmentButtonWidth`                   | dimension     | 25dp       |
| `app:streamAttachmentButtonHeight`                  | dimension     | 25dp       |


- **Send Button**

| Properties                                      | Type          | Default    |
| ----------------------------------------------- | ------------- | ---------- |
| `app:streamInputButtonDefaultIconColor`         | color         | #0076FF    |
| `app:streamInputButtonEditIconColor`            | color         | #0DD25E    |
| `app:streamInputButtonDefaultIconPressedColor`  | color         | WHITE      |
| `app:streamInputButtonDefaultIconDisabledColor` | color         | LIGHT_GRAY |
| `app:streamInputButtonIcon`                     | reference     | -          |
| `app:streamInputButtonWidth`                    | dimension     | 25dp       |
| `app:streamInputButtonHeight`                   | dimension     | 25dp       |
		
- **Input Text**

| Properties                  | Type                  | Default         |
| --------------------------- | --------------------- | --------------- |
| `app:streamInputHint`       | string                | Write a message |
| `app:streamInputTextSize`   | dimension             | 15sp            |
| `app:streamInputTextColor`  | color                 | BLACK           |
| `app:streamInputHintColor`  | color                 | DARK_GRAY       |
| `app:streamInputTextStyle`  | normal, bold, italic  | normal          |



- **Input Background**

| Properties                          | Type        | Default |
| ----------------------------------- | ----------- | ------- |
| `app:streamInputBackground`         | reference   | -       |
| `app:streamInputSelectedBackground` | reference   | -       |
| `app:streamInputEditBackground`     | reference   | -       |

#### Writing your own message input view

You can also create your own message input view. 
Building your own message list or channel list is a lot of work. 
A message input or channel header view is much easier to build though.

**ChannelViewModel**

As a first step connect the channel view model. The channel view model holds all the state for a channel activity.

```java
public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
	this.channelViewModel = viewModel;
	binding.setLifecycleOwner(lifecycleOwner);
	init();
	observeUIs(lifecycleOwner);
}
```

Second step is to forward typing events to the view model

```java
    private void stopTyping() {
        isTyping = false;
        channelViewModel.getChannel().stopTyping();
        if (typingListener != null) {
            typingListener.onStopTyping();
        }
    }

    private void keyStroke() {
        channelViewModel.getChannel().keystroke();
        isTyping = true;
        if (typingListener != null) {
            typingListener.onKeystroke();
        }
    }
```

Third step is to connect the sendMessage flow

TODO document this
