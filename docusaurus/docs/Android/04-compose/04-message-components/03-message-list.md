# MessageList

The `MessageList` component is a crucial part when building a chat experience. We support two versions of the `MessageList` component:

* **ViewModel-powered**: This version connects itself to the `MessageListViewModel` and loads all the required data. It also connects single and long item tap, pagination and bottom reached events to the `ViewModel`.
* **Stateless**: This is a stateless version of the list, which doesn't know about the `ViewModel` and depends on pure state from external sources, to render its UI.

:::note 

The **ViewModel-powered** version of the list uses the **stateless** list internally. That way, when providing the same state to either component, the behavior will be the same. 

Additionally, we cannot provide a default `ViewModel`, as it requires the `channelId` to load the data, so you'll have to build an instance yourself.

:::

Based on the provided state, this component shows the following UI:

* `LoadingView`: If we're loading the initial data.
* `EmptyView`: If there is no data and we've finished loading.
* `Messages`: Shows a list of messages within the channel, including file and image attachments, with various actions like thread clicks, item long taps, pagination and reaching the bottom.

Let's see how to show a list of messages.

## Usage

To use the **ViewModel-powered** `MessagesList`, add it to the rest of your UI, e.g. within `setContent()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // load data

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

            // rest of your UI
        }
    }
}
```

As you can see, it's easy to add the component to the rest of your UI, combine them with our other component or even your own, to build a custom screen. Additionally, if you choose the **ViewModel-powered** version, as seen here, you just need to provide a `MessageListViewModel` and the component will work on its own.

The snippet above will produce the following UI.

 ![The Default MessageList component with a MessageListHeader](../../assets/compose_default_message_list_component.png)

Notice how easy it was to integrate this component with other composable functions, like our `MessageListHeader`. You can see that the component shows messages and their reactions. As mentioned, it also shows attachment messages and handles pagination and reaching the bottom of the list.

Let's see how to handle the actions within the list.

## Handling Actions

The `MessageListComponent` exposes the following actions, as per the signature:

```kotlin
@Composable
fun MessageList(
	..., // state & UI
    onThreadClick: (Message) -> Unit = { viewModel.onMessageThreadClick(it) },
    onLongItemClick: (Message) -> Unit = { viewModel.onMessageSelected(it) },
    onMessagesStartReached: () -> Unit = { viewModel.onLoadMore() },
    onScrollToBottom: () -> Unit = { viewModel.onScrolledToBottom() },
)
```

* `onThreadClick`: Handler when the user taps on a message with a thread.
* `onLongItemClick`: Handler when the user long taps on an item. 
* `onMessagesStartReached`: Handler when the user reaches the end of messages (oldest message) and we need to trigger pagination.
* `onScrollToBottom`: Handler when the user reaches the start of the list (newest message). Used to remove the "New message" or "Scroll to bottom" actions from the UI.

You can customize the behavior here by providing your own actions, like so:

```kotlin
MessageList(
    viewModel = listViewModel,
    modifier = Modifier.fillMaxSize(),
    // actions
    onThreadClick = { message -> },
    onLongItemClick = { message -> },
    onMessagesStartReached = {},
    onScrollToBottom = {}
)
```

If you're using the **ViewModel-powered** version of the component, these actions update the state within the `ViewModel` by default, while the default actions of the **stateless** version are all empty.

If you override the default actions to build your custom behavior, we still recommend storing the data in the `ViewModel`, as most of the behavior like having threads and pagination is already built for you.

We recommend using the **ViewModel-powered** version for ease of use. Alternatively, you can use the stateless version and provide the data manually, for more control.

##  Customization

We allow for two ways of customizing the `MessageList` component, as per the signature:

```kotlin
@Composable
fun MessageList(
	..., // state
    modifier: Modifier = Modifier,
    itemContent: @Composable (Message) -> Unit = {
        DefaultMessageContainer(
            message = it,
            onThreadClick = onThreadClick,
            onLongItemClick = onLongItemClick,
            currentUser = viewModel.currentMessagesState.currentUser
        )
    }
)
```

* `modifier`: Modifier for the root component. Useful for things like the component size, padding, background and similar.
* `itemContent`: Composable function that allows you to fully override the UI and behavior of each message in the list. This function will be applied to each item in the list and you'll gain access to the `Message` inside the lambda, when building your custom UI.

An example of customizing the `Message` items in the list is the following:

```kotlin
@Composable
fun CustomMessageList() {
    MessageList(
        viewModel = listViewModel,
        modifier = Modifier.fillMaxSize(),
        itemContent = { message ->
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .widthIn(max = 300.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(
                        modifier = Modifier.size(36.dp),
                        painter = rememberCoilPainter(request = message.user.image)
                    )

                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = message.user.name,
                        style = ChatTheme.typography.bodyBold,
                        fontSize = 14.sp
                    )
                }

                MessageBubble(
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

These components also use modifiers and other properties to style them and make them look nicer. With a simple custom parameter, you this snippet will produce the following UI.

![Custom MessageList items](../../assets/compose_custom_message_list_component.png)

As per our description, the `Avatar` and the user name `Text` are shown in a `Row`, after which we see the `MessageBubble`.

Using this approach, you can completely customize the items to your needs and you can use click, touch and combined modifiers to customize the touch event behavior.
