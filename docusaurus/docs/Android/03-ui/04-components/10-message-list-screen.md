# Message List Screen

To set up a self-contained chat screen that shows a list of messages and give users the ability to send messages you can use one of the following components:

* `MessageListFragment`: A Fragment that represents a self-contained chat screen.
* `MessageListActivity`: An Activity that is just a thin wrapper around `MessageListFragment`.

`MessageListFragment` contains and initializes three components internally:

* [`MessageListHeaderView`](./04-message-list-header.md): Displays a navigation icon, the name of the channel or thread and a channel avatar.
* [`MessageListView`](./03-message-list.md): Shows a list of paginated messages, with threads, replies, reactions and deleted messages.
* [`MessageInputView`](./05-message-input.md): Handles the message input, as well as attachments and message actions like editing and replying.

**Note**: Fragments and Activities representing self-contained screens are easy to use. They allow you to explore the SDK's features in a breeze, however, they offer limited customization.

## Usage

To use chat screen, you just need to add `MessageListFragment` to your Activity or Fragment:

```kotlin
class MyMessageListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stream_ui_fragment_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MessageListFragment.newInstance(cid = "channelType:channelId"))
                .commit()
        }
    }
}
```

Alternatively, you can simply start `MessageListActivity` from the SDK to achieve similar result with a single line of code:

```kotlin
context.startActivity(MessageListActivity.createIntent(context, cid = "channelType:channelId"))
```

This small snippet will produce a fully working solution, as shown in the image below.

![The ChannelsScreen Component](../../assets/compose_default_channels_screen_component.png)

## Handling Actions

To handle actions supported by `MessageListFragment` you have to implement corresponding click listeners in the parent Fragment or Activity:

```kotlin
class MainActivity : AppCompatActivity(), MessageListFragment.BackPressListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add MessageListFragment to the layout
    }
    
    override fun onBackPress() {
        // Handle back press
    }
}
```

Currently, there is one click listener you can use with the `MessageListFragment`:

* `BackPressListener`: Click listener for the navigation button in the header. Finishes Activity by default.

## Customization
