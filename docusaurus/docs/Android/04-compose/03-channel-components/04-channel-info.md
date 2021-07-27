# ChannelInfo

The `ChannelInfo` component is used to show more information, as well as different actions the user can take, for the currently selected `Channel`.

Internally, the `ChannelInfo` is composed of the following components, organized in a `Column`:

* Two `Text` components that display the name of the channel, as well as the member count.
* A `LazyRow` which displays a row of `ChannelInfoUserItem`s, representing the channel members.
* `ChannelOptions` which show a list of options the user can choose from, for the channel.

It also exposes an action when the user selects any channel option from the list. Let's see how to use the `Channelnfo` in your code and how it behaves.

## Usage

If you're using the `ChannelScreen` component, you don't have to do anything. The `ChannelInfo` component and its logic will be integrated into the UI.

If you're looking to build a custom UI, you can simply add the `ChannelInfo` component on top of your UI, within a `Box`, like so:

```kotlin
@Composable
fun MyCustomUi() {
    // data for the component
    val user by listViewModel.user.collectAsState()
    val selectedChannel = listViewModel.selectedChannel

    Box(modifier = Modifier.fillMaxSize()) {
        // Rest of your content

        if (selectedChannel != null) {
            ChannelInfo(
                modifier = Modifier // aligning the content to the bottom
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter),
                selectedChannel = selectedChannel,
                user = user,
                onChannelOptionClick = { listViewModel.onChannelAction(it) }
            )
        }
    }
}
```

For the `ChannelInfo` component to work, you need to provide a the `selectedChannel` and `user` parameters, as `Channel` and `User` objects, respectively.

In the example above, you fetch the data from a `ChannelListViewModel`, that you use in the rest of the UI. But you can also provide the data manually, if you decide not to use our components, like the `ChannelList`.

Notice how you also show the `ChannelInfo`, only if the `selectedChannel` is not null. This is a smart way of knowing when to show the info and when to hide it.

With a bit of extra code for the rest of the content, when selecting a channel, the snippet above will produce the next UI:

![The ChannelInfo Component](../../assets/compose_default_channel_info_component.png)

This just represents the `ChannelInfo` component, the rest of the UI can be whatever your implementation requires. In the header you can see information about the `Channel` members, as well as how many members are in the channel and how many are online.

The header is followed by a row of `ChannelInfoUserItem`s, which show more info about each member.

Finally, you can see a list of `ChannelOption`s, which are different based on if you're an admin for this channel or just a member. Each action has an icon, a title and clicking it will propagate the action back to the `ChannelInfo` call site.

Let's see how to handle the channel actions.

## Handling Actions

The `ChannelInfo` exposes one action you can handle, as per the signature:

```kotlin
@Composable
fun ChannelInfo(
    ..., // state & styling,
    onChannelOptionClick: (ChannelListAction) -> Unit,
)
```

* `onChannelOptionClick`: Handler when the user taps on any channel option in the list.

By providing this handler, you can choose what happens when the user selects options like "Leave Group", "Delete Conversation", "Cancel" and more. You can react to these actions, update your UI state and show new UI if needed.

An example of providing a handler can be seen here:

```kotlin
ChannelInfo(
    ..., // state and styling
    onChannelOptionClick = { action ->
        if (action is ViewInfo) {
            startActivity(ChannelInfoActivity.newInstance(this, action.channel.id))
        } else {
            listViewModel.onChannelAction(action)
        }
    }
)
```

In the snippet above, you customize the `onChannelOptionClick` handler, to open a new `Activity` if the user decides to view more info, otherwise, you send the action to the `listViewModel` and store it to update the state.

## Customization

This component doesn't offer much customization, as per the signature:

```kotlin
@Composable
fun ChannelInfo(
	..., // state and actions
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
)
```

* `modifier`: Used to style the root component of the `ChannelInfo`, which is a `Card` component. Useful for the component size and padding, background, alignment and more.
* `shape`: Used for the `Card` shape. By default, we position the element at the bottom of the screen with the top corners being rounded, which imitates a bottom drawer component. If you want to use the `ChannelInfo` as a dialog, you can change the shape to have all round corners, or to be flat.

An example of customizing this component, to imitate a dialog is the following:

```kotlin
ChannelInfo(
    modifier = Modifier
        .padding(16.dp) // adding padding to the component
        .wrapContentWidth() // wrap width and height
        .wrapContentHeight()
        .align(Alignment.Center), // centering the component
    shape = RoundedCornerShape(16.dp), // rounded corners for all sides
    ... // state
)
```

In this example, we centered the component on the screen, made it wrap its content, added some padding and rounded corners for all sides. This code will produce the following UI:
![The ChannelInfo Component](../../assets/compose_custom_channel_info_component.png)

The `ChannelInfo` component now looks more like a dialog, that displays over other elements. This is just an example of UI customization, the component still isn't a dialog, with a special scrim or dismiss actions.
