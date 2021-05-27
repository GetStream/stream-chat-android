---
id: ui-sample-chat-customizations-changing-message-input-view
title: Changing Message Input View

sidebar_position: 2
---
`MessageInputView` can be customized in two ways: Using XML and programmatically.

## Using XML
Many attributes from this view can be configured, like changing its color, the border and the color of the message input, fonts, components visibility... The whole list can be found [here](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/attrs_message_input_view.xml).

One example of configuration:

```xml
<io.getstream.chat.android.ui.message.input.MessageInputView
    android:id="@+id/messageInputView"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:background="@color/grey_light"
    app:streamUiMessageInputEditTextBackgroundDrawable="@drawable/stream_ui_shape_edit_text_squared"
    app:streamUiMessageInputTextStyle="italic"
    app:streamUiMessageInputDividerBackgroundDrawable="@drawable/stream_ui_divider_green"
    app:streamUiMessageInputTextColor="@color/stream_ui_white"
    />
```

Will create this version of the `MessageInputView`:

![example1 1](../../../assets/message_input_view_example1.jpeg)

With a different configuration:
```xml
<io.getstream.chat.android.ui.message.input.MessageInputView
    android:id="@+id/messageInputView"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/messageListView"
    app:streamUiMessageInputEditTextBackgroundDrawable="@drawable/stream_ui_shape_edit_text_rounded"
    app:streamUiMessageInputTextStyle="italic"
    android:background="@color/stream_ui_black"
    app:streamUiMessageInputDividerBackgroundDrawable="@drawable/stream_ui_divider_green"
    app:streamUiMessageInputTextColor="@color/stream_ui_white"
    />
```

we get:

![example2 1](../../../assets/message_input_view_example2.jpeg)

And different configurations can be used to achieve the desired appearance of `MessageListView`. If you don't have the need to change this View appearance at runtime, XML should be enough. But if you need to able to customize it at runtime, then you can use `MessageInputViewStyle` as described in the next section.

## Configuring programmatically using `MessageInputViewStyle`
Many views in this SDK can be configured by changing the configuration class inside [TransformStyle](https://github.com/GetStream/stream-chat-android/blob/develop/stream-chat-android-ui-components/src/main/kotlin/io/getstream/chat/android/ui/TransformStyle.kt).

Just change the instance of `messageInputStyleTransformer`. Example:

```kotlin
TransformStyle.messageInputStyleTransformer = StyleTransformer { viewStyle ->
    viewStyle.copy(
        messageInputTextColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_white)
    )
}
```
NOTE: The transformer should be set before the view is rendered to make sure that the new style was applied.
