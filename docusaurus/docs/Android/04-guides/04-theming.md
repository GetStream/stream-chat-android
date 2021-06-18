# Theming

Many parts of the UI components can be changed and customized. It is possible to change colors, fonts, sizes and many other aspects of the `Views` to match a desired look. 

## Global Styling

If you want to customize view styles programmatically you can override the corresponding `StyleTransformer` in the `TransformStyle` class.

:::caution
This doesn't apply the changes instantly, the view must be recreated for the changes to take effect.
:::

Here are some examples of styling you can perform here:

**Change avatar image style:**

```kotlin
TransformStyle.avatarStyleTransformer = StyleTransformer { avatarStyle ->
    avatarStyle.copy(
        onlineIndicatorColor = Color.BLUE,
        // more overrides here
    )
}
```
**Change channel list style.** It is possible change many aspects of the list item of the channels.
 
```kotlin
TransformStyle.channelListStyleTransformer = StyleTransformer { channelListViewStyle ->
    channelListViewStyle.copy(
        optionsEnabled = false,
        // more overrides here
    )
}
```

**Message list style.** You can change the list itself, the message, enable/disable functionalities and customize many parts of it.

```kotlin
TransformStyle.messageListStyleTransformer = StyleTransformer { messageListViewStyle ->
    messageListViewStyle.copy(
        backgroundColor = Color.BLUE,
        // more overrides here
    )
}
```

**Message input style:**

```kotlin
TransformStyle.messageInputStyleTransformer = StyleTransformer { messageInputViewStyle ->
    messageInputViewStyle.copy(
        backgroundColor = Color.BLUE,
        // more overrides here
    )
}
```

**Scroll button style:**

```kotlin
TransformStyle.scrollButtonStyleTransformer = StyleTransformer { scrollButtonViewStyle ->
    scrollButtonViewStyle.copy(
        scrollButtonColor = Color.BLUE,
        // more overrides here
    )
}
```

**Reaction view style**.  Customize the reactions of messages.


```kotlin
TransformStyle.viewReactionsStyleTransformer = StyleTransformer { viewReactionsViewStyle ->
    viewReactionsViewStyle.copy(
        bubbleColorMine = Color.BLUE,
        // more overrides here
    )
}
```

**Edit reaction view style**.  Customize the popup menu for reactions.


```kotlin
TransformStyle.editReactionsStyleTransformer = StyleTransformer { editReactionsViewStyle ->
    editReactionsViewStyle.copy(
        bubbleColorMine = Color.BLUE,
        // more overrides here
    )
}
```
