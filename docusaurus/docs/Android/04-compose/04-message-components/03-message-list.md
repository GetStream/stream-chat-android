# MessageList

The `MessageList` component is a crucial part when building a chat experience. We support two versions of the `MessageList` component:

* **Bound**: This version binds itself to the `MessageListViewModel` and loads all the required data. It also connects single and long item tap, pagination, and bottom reached events to the `ViewModel`.
* **Stateless**: This is a stateless version of the list, which doesn't know about the `ViewModel` and depends on pure state from external sources, to render its UI.

:::note 
The **bound** version of the list uses the **stateless** list internally. That way, when providing the same state to either component, the behavior will be the same. 

Additionally, we cannot provide a default `ViewModel` for this component, as it requires the `channelId` to load the data, so you'll have to build an instance yourself.
:::

Based on the provided state, this component shows the following UI:

* `LoadingView`: If we're loading the initial data.
* `EmptyView`: If there is no data and we've finished loading.
* Messages: The list of messages in the channel, including file and image attachments, with various actions like thread clicks, item long taps, pagination and reaching the bottom.

Let's see how to show a list of messages.

## Usage

To use the **bound** `MessageList`, add it to the rest of your UI, e.g. within `setContent()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Load data

    setContent {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            MessageListHeader(...)

            MessageList(
                viewModel = listViewModel,
                modifier = Modifier.fillMaxSize()
            )

            // Rest of your UI
        }
    }
}
```

As you can see, it's easy to add the component to your UI, and to combine them it with our other components (or your own) to build a custom screen. Additionally, if you choose the **bound** version, as seen here, you just need to provide a `MessageListViewModel` and the component will work on its own.

The snippet above will produce the following UI.

 ![The Default MessageList component with a MessageListHeader](../../assets/compose_default_message_list_component.png)

Notice how easy it was to integrate this component with other composable functions, like our `MessageListHeader`. You can see that the component shows different types of messages, such as link and image previews.  It also handles pagination and various other events when scrolling or receiving new messages.

Let's see how to handle the actions within the list.

## Handling Actions

The `MessageList` component exposes the following actions, as per the signature:

```kotlin
@Composable
fun MessageList(
	..., // State & UI
    onThreadClick: (Message) -> Unit = { viewModel.onMessageThreadClick(it) },
    onLongItemClick: (Message) -> Unit = { viewModel.onMessageSelected(it) },
    onMessagesStartReached: () -> Unit = { viewModel.onLoadMore() },
    onScrollToBottom: () -> Unit = { viewModel.onScrolledToBottom() },
)
```

* `onThreadClick`: Handler for the user tapping on a message with a thread.
* `onLongItemClick`: Handler for the user long tapping on an item. 
* `onMessagesStartReached`: Handler for the user reaching the end of messages (oldest message), to trigger pagination.
* `onScrollToBottom`: Handler for the user reaching the start of the list (newest message). Used to remove the "New message" or "Scroll to bottom" actions from the UI.

You can customize the behavior here by providing your own actions, like so:

```kotlin
MessageList(
    viewModel = listViewModel,
    modifier = Modifier.fillMaxSize().background(ChatTheme.colors.appBackground),
    // Actions
    onThreadClick = { message -> },
    onLongItemClick = { message -> },
    onMessagesStartReached = { },
    onScrollToBottom = { }
)
```

If you're using the **bound** version of the component, these actions update the state within the `ViewModel` by default, while the default actions of the **stateless** version are all empty.

If you override the default actions to build your custom behavior, we still recommend storing the data in the `ViewModel`, as most of the behavior like having threads and pagination is already built for you.

We recommend using the **bound** version for ease of use. Alternatively, you can use the stateless version and provide the data manually, for more control.

##  Customization

We allow for two ways of customizing the `MessageList` component, as per the signature:

```kotlin
@Composable
fun MessageList(
	..., // State
    modifier: Modifier = Modifier,
    itemContent: @Composable (Message) -> Unit = { message ->
        DefaultMessageContainer(
            message = message,
            onThreadClick = onThreadClick,
            onLongItemClick = onLongItemClick
        )
    }
)
```

* `modifier`: Modifier for the root component. Useful for things like the component size, padding, background and similar.
* `itemContent`: Composable function that allows you to fully override the UI and behavior of each message in the list. This function will be applied to each item in the list and you'll gain access to the `MessageItem` inside the lambda when building your custom UI.

Here's an example of customizing the `Message` items in the list:

```kotlin
@Composable
fun CustomMessageList() {
    MessageList(
        viewModel = listViewModel,
        modifier = Modifier.fillMaxSize().background(ChatTheme.colors.appBackground),
        itemContent = { messageItem ->
		    val (message, position) = messageItem
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .widthIn(max = 300.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(
                        modifier = Modifier.size(36.dp),
                        painter = rememberImagePainter(data = message.user.image)
                    )

                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = message.user.name,
                        style = ChatTheme.typography.bodyBold,
                        fontSize = 14.sp
                    )
                }

                MessageBubble(
                    color = ChatTheme.colors.barsBackground,
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(
                        topEnd = 16.dp,
                        topStart = 0.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    ),
                    content = {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = message.text
                        )
                    }
                )
            }
        }
    )
}
```

This snippet of code prepares a custom message item component, using the `itemContent` parameter. It features a `Column` that hosts a `Row` with an avatar and a text showing the user name, as well as a `MessageBubble` that wraps the message text.

These components also use modifiers and other properties to style them and make them look nicer. With a simple parameter, this snippet will now produce the following UI in the `MessageList`:

![Custom MessageList items](../../assets/compose_custom_message_list_component.png)

As per our description, the `Avatar` and the user name `Text` are shown in a `Row`, after which we see the `MessageBubble`. Note that this approach doesn't automatically display attachments, so you'll have to show attachment UI based on the provided `attachmentFactories` within the `ChatTheme`.

Using this approach, you can completely customize the items to your needs and you can use click, touch and combined modifiers to customize the touch event behavior.
