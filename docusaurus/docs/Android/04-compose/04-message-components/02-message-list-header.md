# MessageListHeader

The `MessageListHeader` component is a clean, stateless component, that doesn't require a `ViewModel`. It consists of three elements:

* `BackButton`: Displays the back arrow icon and lets the user navigate to the previous screen.
* Header title: Shows either more information about the channel, or a `NetworkLoadingView`, based on whether the network is available or not.
* `Avatar`: Shows the channel or conversation image.

Let's see how to use this in your UI.

## Usage

To use the component, simply combine it with the rest of your UI, e.g. in `setContent`, in you `Activity` or `Fragment`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Load the data for the header here
    
    setContent {
        Column(Modifier.fillMaxSize()) {
            MessageListHeader(
                channel,
                currentUser,
                isNetworkAvailable,
                messageMode,
                modifier = Modifier.fillMaxWidth(),
                onBackPressed = { },
                onHeaderActionClick = { },
            )

            // Rest of your UI
        }
    }
}
```

This component doesn't have its own `ViewModel`, as it's stateless. You need to provide the data to the header, otherwise it doesn't know what to render.

We recommend using either our `ChatDomain` directly and combining information you get from it, or the `MessageListViewModel`, in pair with the rest of our components, for ease of use.

The snippet above, after providing the data, generates the following UI:

![Default MessagesScreen component](../../assets/compose_default_message_list_header_component.png)

Now let's see how to handle the header actions.

## Handling Actions

The `MessageListHeader` exposes two actions, as per the signature:

```kotlin
@Composable
fun MessageListHeader(
    ..., // State
    onBackPressed: () -> Unit,
    onHeaderActionClick: () -> Unit,
)
```

* `onBackPressed`: Handler for the `BackButton` click action.
* `onHeaderActionClick`: Handler when the user clicks on the title for more information about the channel.

To customize these actions, simply use the `MessageListHeader` with the rest of your UI components, like within `setContent`, and pass in the required actions:

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load required data

        setContent {
            Column(Modifier.fillMaxSize()) {
                MessageListHeader(
                    ..., // State
                    onBackPressed = { finish() },
                    onHeaderActionClick = { 
                      // Show your custom UI
                    })

                // Rest of your UI
            }
        }
    }
```

This way it's easy to combine the actions from this component with your custom UI and logic.

## Customization

As the component is fully state-dependent, you can customize the data it shows, as per the signature:

```kotlin
@Composable
fun MessageListHeader(
    channel: Channel,
    currentUser: User?,
    isNetworkAvailable: Boolean,
    messageMode: MessageMode,
    modifier: Modifier = Modifier,
    ... // actions
)
```

* `channel`: The information about the current channel, used to show the member count, name and avatar data.
* `currentUser`: Currently logged in user, used to differentiate it from other users, when loading the channel image.
* `isNetworkAvailable`: Used to switch between the `NetworkLoadingView` and the member count text.
* `messageMode`: Used to determine the header title. If we're in a thread, we show a title saying who the owner of the parent message is.
* `modifier`: Applied to the root component of the header. Useful for padding and height.

These will change what data is displayed in the header. 

<!-- TODO WIP PAGE If you want to build a fully custom header, read our [Building Custom Screens](../07-guides/06-building-custom-screens.md) guide. -->
