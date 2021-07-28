# AttachmentFactory

The `AttachmentFactory` is a class that allows you to build your own attachment factories. It exposes the following properties and behavior:

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
* `predicate`: Lambda function that accepts a message and returns if a given factory can consume the message to show an attachment.
* `canHandle()`: A function that is called when showing messages. It validates the message using all defined factories and it chooses the one that can consume it.

There are three examples of default attachment factory implementations, in the **StreamAttachmentFactories.kt** file:

```kotlin
public val defaultFactories: List<AttachmentFactory> = listOf(
    AttachmentFactory(
        { state -> LinkAttachmentFactory(state) },
        { message -> message.attachments.any { it.ogUrl != null } }
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
