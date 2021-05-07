---
id: uiGlobalCustomizationsTransformStyle
title: TransformStyle
sidebar_position: 1
---

The SDK provides two entry points for global customization:
- The [ChatUI](https://github.com/GetStream/stream-chat-android/blob/develop/stream-chat-android-ui-components/src/main/kotlin/io/getstream/chat/android/ui/ChatUI.kt) class to override such things as fonts, url signing logic, etc.
- The [TransformStyle](https://github.com/GetStream/stream-chat-android/blob/develop/stream-chat-android-ui-components/src/main/kotlin/io/getstream/chat/android/ui/TransformStyle.kt) class to override view attributes programmatically.

## Global View Style Customization

If you want to customize view styles programmatically you can override corresponding `StyleTransformer` in the `TransformStyle` class.

> **Important: This doesn't apply the changes instantly, the view must be recreated for the
changes to take effect.**

Example:

```kotlin
TransformStyle.avatarStyleTransformer = StyleTransformer { avatarStyle ->
    avatarStyle.copy(
        onlineIndicatorColor = Color.BLUE,
        // more overrides here
    )
}
TransformStyle.channelListStyleTransformer = StyleTransformer { channelListViewStyle ->
    channelListViewStyle.copy(
        optionsEnabled = false,
        // more overrides here
    )
}
TransformStyle.messageListStyleTransformer = StyleTransformer { messageListViewStyle ->
    messageListViewStyle.copy(
        backgroundColor = Color.BLUE,
        // more overrides here
    )
}
TransformStyle.messageListItemStyleTransformer = StyleTransformer { messageListItemStyle ->
    messageListItemStyle.copy(
        messageBackgroundColorMine = Color.BLUE,
        // more overrides here
    )
}
TransformStyle.messageInputStyleTransformer = StyleTransformer { messageInputViewStyle ->
    messageInputViewStyle.copy(
        backgroundColor = Color.BLUE,
        // more overrides here
    )
}
TransformStyle.scrollButtonStyleTransformer = StyleTransformer { scrollButtonViewStyle ->
    scrollButtonViewStyle.copy(
        scrollButtonColor = Color.BLUE,
        // more overrides here
    )
}
TransformStyle.viewReactionsStyleTransformer = StyleTransformer { viewReactionsViewStyle ->
    viewReactionsViewStyle.copy(
        bubbleColorMine = Color.BLUE,
        // more overrides here
    )
}
TransformStyle.editReactionsStyleTransformer = StyleTransformer { editReactionsViewStyle ->
    editReactionsViewStyle.copy(
        bubbleColorMine = Color.BLUE,
        // more overrides here
    )
}
```
