# Global Styling

If you want to customize view styles programmatically you can override the corresponding `StyleTransformer` in the `TransformStyle` class.

> **Important: This doesn't apply the changes instantly, the view must be recreated for the
changes to take effect.**

Here are some examples of styling you can perform here:

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
