# ChatTheme

The `ChatTheme` component is a wrapper that **you should use as the root** of all Compose UI Components. It's used to provide the default properties that help us style the application, such as:

* **Colors**: Defines a palette of colors we support in the app. These are applied to all components and provide us with a dark/light mode by default, but can be used to override the design system completely.
* **Typography**: Used for all text elements, to apply different text styles to each component. Can be used to change the typography completely.
* **Shapes**: Defines several shapes we use across our Compose UI components. Can be used to change the shape of messages, input fields, avatars and attachments.
* **AttachmentFactories**: Used to process messages and show different types of attachment UI, given the provided factories. Can be used to override the UI for file, image and link attachments, as well as to add custom attachment types.
* **ReactionTypes**: Used to define the supported message reactions in the app. You can use the default-provided reactions, or customize them to your app's needs.
* **DateFormatter**: Used to define the timestamp formatting in the app. You can use the default formatting, or customize it to your needs.

:::note
In case any of these properties are not provided, because you're not using the `ChatTheme` to wrap our Compose UI Components, you'll get an exception saying that required properties are missing. 
:::

Let's see how to use the `ChatTheme` and how to customize the UI within.

## Usage

To use the `ChatTheme`, simply wrap your UI content with it, like in the following example:

```kotlin {6,13}
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val channelId = intent.getStringExtra(KEY_CHANNEL_ID) ?: return

    setContent {
        ChatTheme {
            MessagesScreen(
                channelId = channelId,
                messageLimit = 30,
                onBackPressed = { finish() },
                onHeaderActionClick = {}
            )
        }
    }
}
```

The `ChatTheme` provides default implementations for all its styling properties. That way, you can keep using our default color palette, typography, shapes, attachment factories and reaction types.

All you have to do is pass in the UI content you want to show, within its trailing lambda. This snippet above will produce the following UI. You'll also notice that if you switch to the dark theme in your system UI, the app will re-draw accordingly.

| Light theme | Dark theme |
| --- | --- |
| ![Default MessagesScreen component](../../assets/compose_default_messages_screen_component.png) | ![Default Dark Mode MessagesScreen Component](../../assets/compose_default_messages_screen_component_dark.png) |

Let's see how to customize the theme.

## Customization

To customize the `ChatTheme`, simply override any of the default properties by passing in your custom design style, like so:

```kotlin
setContent {
    ChatTheme(
        shapes = StreamShapes( // Customizing the shapes
            avatar = RoundedCornerShape(8.dp),
            attachment = RoundedCornerShape(16.dp),
            inputField = RectangleShape,
            myMessageBubble = RoundedCornerShape(16.dp),
            otherMessageBubble = RoundedCornerShape(16.dp)
        )
    ) {
        MessagesScreen(
            channelId = channelId,
            messageLimit = 30,
            onBackPressed = { finish() },
            onHeaderActionClick = {}
        )
    }
}
```

In the snippet above, we customized the shapes to be different from the default values. We made both the message types rounded, the input field rectangular and changed the avatar to not be circular.

This snippet above will produce the following screen:

![Custom ChatTheme Component](../../assets/compose_custom_chat_theme_component.png)

You can see how the input field is rectangular, how all messages have rounded corners, regardless of their position in the list or who sent the message. Finally, the avatars are now a squircle, instead of a circle.

It's really easy to customize these properties or provide static customization that you just reuse all over your app.

Let's see what each property exposes and what the values are used for.

### StreamColors

`StreamColors` are used to represent all the colors we use and apply to our components in the SDK.

You can find the definitions of all the colors we expose in [the class documentation](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/theme/StreamColors.kt), as well as what the default provided colors are.

You can also browse which components are using the colors, to know what will be affected by any change.

### StreamTypography

`StreamTypography` is used to apply different font weights and sizes to our textual UI components.

You can find all the text style properties we expose in [the class documentation](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/theme/StreamTypography.kt), as well as what the default styles are.

You can also browse which components are using the styles, to know what will be affected by any change.

### StreamShapes

`StreamShapes` provides a small collection of shapes that let us style our containers.

You can find all the shapes we expose in [the class documentation](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/theme/StreamShapes.kt), as well as what the default shapes are.

These are really easy to customize, as you've seen before, and can make your app feel closer to your design system.
