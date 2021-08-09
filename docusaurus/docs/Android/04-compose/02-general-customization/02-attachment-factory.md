# Custom Attachments

The `AttachmentFactory` class allows you to build your own attachments to display in the [Message List](../04-message-components/03-message-list.md). It exposes the following properties and behavior:

```kotlin
public class AttachmentFactory(
    public val factory: @Composable (AttachmentState) -> Unit,
    private val predicate: (Message) -> Boolean,
) {
    // Validator to check if we can handle the given message
    public fun canHandle(message: Message): Boolean {
        return predicate(message)
    }
}
```

* `factory`: Defines the composable function that accepts an `AttachmentState` and shows any given attachment component.
* `predicate`: Lambda function that accepts a message and returns `true` if a given factory can consume the message to show an attachment. The first factory in the list of available factories that can handle the message will be used to render its attachments.

There are three examples of default attachment factory implementations, in the [`StreamAttachmentFactories.kt` file](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/theme/StreamAttachmentFactories.kt):

```kotlin
public val defaultFactories: List<AttachmentFactory> = listOf(
    AttachmentFactory(
        { state -> LinkAttachmentFactory(state) },
        { message -> message.attachments.any { it.titleLink != null } }
    ),
    AttachmentFactory(
        { state -> ImageAttachmentFactory(state) },
        { message -> message.attachments.all { it.type == "image" } }
    ),
    AttachmentFactory(
        { state -> FileAttachmentFactory(state) },
        { message -> message.attachments.any { it.type != "image" } }
    )
)
```

These factories perform specific checks, like if the `ogUrl` exists, giving us information that this is a link attachment, or if all of the message attachments are of the `"image"` type.

Each of these factories supplies a validator, as well as a factory composable lambda function that calls `LinkAttachmentFactory`, `ImageAttachmentFactory` or `FileAttachmentFactory`.

To customize the factories your code uses, you can always override the `attachmentFactories` parameter of the `ChatTheme` wrapper:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val defaultFactories = StreamAttachmentFactories.defaultFactories

    setContent {
        // override the default factories by adding your own
        ChatTheme(attachmentFactories = myAttachmentFactories + defaultFactories) {
            ChannelsScreen(...)
        }
    }
}
```

That way, you can build any type of factories you want, to show things like the user location within a Google Maps component, audio files, videos and more.
