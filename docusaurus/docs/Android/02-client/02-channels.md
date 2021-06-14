# Channels

<!-- TODO: Add brief intro about what channels are -->

## Querying Channel List

You can query channels based on built-in fields as well as any custom field you add to channels. Multiple filters can be combined using AND, OR logical operators, each filter can use its comparison (equality, inequality, greater than, greater or equal, etc.). You can find the complete list of supported operators in the [query syntax section](https://getstream.io/chat/docs/react/query_syntax/) of the docs.

As an example, let's say that you want to query the last conversations I participated in sorted by `last_message_at`.

Here’s an example of how you can query the list of channels:

```kotlin
val request = QueryChannelsRequest(
    filter = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf("thierry")),
    ),
    offset = 0,
    limit = 10,
    querySort = QuerySort.desc("last_message_at")
).apply {
    watch = true
    state = true
}

client.queryChannels(request).enqueue { result ->
    if (result.isSuccess) {
        val channels: List<Channel> = result.data()
    } else {
        // Handle result.error()
    }
}
```

:::note
At a minimum, the filter should include members: { $in: [userID] }.
:::

On messaging and team applications you normally have users added to channels as a member. A good starting point is to use this filter to show the channels the user is participating.

```kotlin
val filter = Filters.`in`("members", listOf("thierry"))
```

On a support chat, you probably want to attach additional information to channels such as the support agent handling the case and other information regarding the status of the support case (ie. open, pending, solved).

```kotlin
val filter = Filters.and(
    Filters.eq("agent_id", user.id),
    Filters.`in`("status", listOf("pending", "open", "new")),
)
```

### Paginating Channels

Query channel requests can be paginated similar to how you paginate on other calls. Here's a short example:

```kotlin
// Get the first 10 channels
val filter = Filters.`in`("members", "thierry")
val offset = 0
val limit = 10
val request = QueryChannelsRequest(filter, offset, limit)
client.queryChannels(request).enqueue { result ->
    if (result.isSuccess) {
        val channels = result.data()
    } else {
        // Handle result.error()
    }
}

// Get the second 10 channels
val nextRequest = QueryChannelsRequest(
    filter = filter,
    offset = 10, // Skips first 10
    limit = limit
)
client.queryChannels(nextRequest).enqueue { result ->
    if (result.isSuccess) {
        val channels = result.data()
    } else {
        // Handle result.error()
    }
}
```

### Paginating Channel Messages

The channel query endpoint allows you to paginate the list of messages, watchers, and members for one channel. To make sure that you can retrieve a consistent list of messages, pagination does not work with simple offset/limit parameters but instead, it relies on passing the ID of the messages from the previous page.

For example: say that you fetched the first 100 messages from a channel and want to lead the next 100. To do this you need to make a channel query request and pass the ID of the oldest message if you are paginating in descending order or the ID of the newest message if paginating in ascending order.

Use the `id_lt` parameter to retrieve messages older than the provided ID and `id_gt` to retrieve messages newer than the provided ID.

The terms `id_lt` and `id_gt` stand for ID less than and ID greater than.

ID-based pagination improves performance and prevents issues related to the list of messages changing while you’re paginating. If needed, you can also use the inclusive versions of those two parameters: `id_lte` and `id_gte`.

```kotlin
val channelClient = client.channel("messaging", "general")
val pageSize = 10

// Request for the first page
val request = QueryChannelRequest()
    .withMessages(pageSize)

channelClient.query(request).enqueue { result ->
    if (result.isSuccess) {
        val messages: List<Message> = result.data().messages
        if (messages.size < pageSize) {
            // All messages loaded
        } else {
            // Load next page
            val nextRequest = QueryChannelRequest()
                .withMessages(LESS_THAN, messages.last().id, pageSize)
            // ...
        }
    } else {
        // Handle result.error()
    }
}
```

For members and watchers, we use limit and offset parameters.

:::note
The maximum number of messages that can be retrieved at once from the API is 300.
:::

## Watching Channels

The call to `channel.watch` does a few different things in one API call:

- It creates the channel if it doesn't exist yet (if this user has the right permissions to create a channel)
- It queries the channel state and returns members, watchers and messages
- It watches the channel state and tells the server that you want to receive events when anything in this channel changes

### Watching a Channel and Receiving Events

The examples below show how to watch a channel.

```kotlin
val channelClient = client.channel(channelType = "messaging", channelId = "general")

channelClient.watch().enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}
```
:::note
Watching a channel only works if you have connected as a user to the chat API
:::

The default `queryChannels` API returns channels and starts watching them. There is no need to also use `channel.watch` on the channels returned from `queryChannels`.

```kotlin
val request = QueryChannelsRequest(
    filter = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf(currentUserId)),
    ),
    offset = 0,
    limit = 10,
    querySort = QuerySort.desc("last_message_at")
).apply {
    // Watches the channels automatically
    watch = true
    state = true
}

// Run query on ChatClient
client.queryChannels(request).enqueue { result ->
    if (result.isSuccess) {
        val channels: List<Channel> = result.data()
    } else {
        // Handle result.error()
    }
}
```

### Stop Receiving Channel Events

To stop receiving channel events:

```kotlin
channelClient.stopWatching().enqueue { result ->
    if (result.isSuccess) {
        // Channel unwatched
    } else {
        // Handle result.error()
    }
}
```

### Getting the Watcher Count

To get the watcher count of a channel:

```kotlin
val request = QueryChannelRequest().withState()
channelClient.query(request).enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
        channel.watcherCount
    } else {
        // Handle result.error()
    }
}
```

```kotlin
val request = QueryChannelRequest()
    .withWatchers(limit = 5, offset = 0)
channelClient.query(request).enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
        val watchers: List<User> = channel.watchers
    } else {
        // Handle result.error()
    }
}
```

### Listening to Watching Events

A user already watching the channel can listen to users starting and stopping watching the channel with the real-time events:

```kotlin
// Start watching channel
channelClient.watch().enqueue {
    /* Handle result */
}

// Subscribe for watching events
channelClient.subscribeFor(
    UserStartWatchingEvent::class,
    UserStopWatchingEvent::class,
) { event ->
    when (event) {
        is UserStartWatchingEvent -> {
            // User who started watching the channel
            val user = event.user
        }
        is UserStopWatchingEvent -> {
            // User who stopped watching the channel
            val user = event.user
        }
    }
}
```


## Creating Channels

Both channel `channel.query` and `channel.watch` methods ensure that a channel exists and create one otherwise. If all you need is to ensure that a channel exists, you can use `channel.create`.

## Creating a Channel Using a Channel Id

```kotlin
val channelClient = client.channel(channelType = "messaging", channelId = "general")

channelClient.create().enqueue { result ->
    if (result.isSuccess) {
        val newChannel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}
```

## Creating a Channel for a List of Members

Channels can be used to conversations between users. In most cases, you want conversations to be unique and make sure that a group of users have only a channel.

You can achieve this by leaving the channel ID empty and provide channel type and members. When you do so, the API will ensure that only one channel for the members you specified exists (the order of the members does not matter).

:::note
You cannot add/remove members for channels created this way.
:::

```kotlin
client.createChannel(
    channelType = "messaging",
    members = listOf("thierry", "tomasso")
).enqueue { result ->
    if (result.isSuccess) {
        val channel = result.data()
    } else {
        // Handle result.error()
    }
}
```

## Updating Channels

There are two ways to update a channel using the Stream API - a partial or full update. A partial update will retain any custom key-value data, whereas a complete update is going to remove any that are unspecified in the API request.

### Partial Update

A partial update can be used to set and unset specific fields when it is necessary to retain additional custom data fields on the object. AKA a patch style update.

```kotlin
// Here's a channel with some custom field data that might be useful
val channelClient = client.channel(channelType = "messaging", channelId = "general")

channelClient.create(
    members = listOf("thierry", "tomasso"),
    extraData = mapOf(
        "source" to "user",
        "source_detail" to mapOf("user_id" to 123),
        "channel_detail" to mapOf(
            "topic" to "Plants and Animals",
            "rating" to "pg"
        )
    )
).execute()

// let's change the source of this channel
channelClient.updatePartial(set = mapOf("source" to "system")).execute()

// since it's system generated we no longer need source_detail
channelClient.updatePartial(unset = listOf("source_detail")).execute()

// and finally update one of the nested fields in the channel_detail
channelClient.updatePartial(set = mapOf("channel_detail.topic" to "Nature")).execute()

// and maybe we decide we no longer need a rating
channelClient.updatePartial(unset = listOf("channel_detail.rating")).execute()
```

### Full Update

The `updateChannel` function updates all of the channel data. <b>Any data that is present on the channel and not included in a full update will be deleted.</b>

```kotlin
val channelClient = client.channel("messaging", "general")

channelClient.update(
    message = Message(
        text = "Thierry changed the channel color to green"
    ),
    extraData = mapOf(
        "name" to "myspecialchannel",
        "color" to "green",
    ),
).enqueue { result ->
    if (result.isSuccess) {
        val channel = result.data()
    } else {
        // Handle result.error()
    }
}
```

## Channel Members

### Querying Members

Sometimes channels will have many hundreds (or thousands) of members and it is important to be able to access ID's and information on all of these members. The `queryMembers` endpoint queries the channel members and allows the user to paginate through a full list of users in channels with very large member counts. The endpoint supports filtering on numerous criteria to efficiently return member information.

:::note
The members are sorted by _created_at_ in ascending order.
:::

:::note
_Stream Chat_ does not run MongoDB on the backend, only a subset of the query options are available.
:::

Here’s some example of how you can query the list of members:

```kotlin
val channelClient = client.channel("messaging", "general")

val offset = 0 // Use this value for pagination
val limit = 10
val sort = QuerySort<Member>()

// Channel members can be queried with various filters
// 1. Create the filter, e.g query members by user name
val filterByName = Filters.eq("name", "tommaso")
// 2. Call queryMembers with that filter
channelClient.queryMembers(offset, limit, filterByName, sort).enqueue { result ->
    if (result.isSuccess) {
        val members: List<Member> = result.data()
    } else {
        Log.e(TAG, String.format("There was an error %s", result.error()), result.error().cause)
    }
}

// Here are some other commons filters you can use:

// Autocomplete members by user name (names containing "tom")
val filterByAutoCompleteName = Filters.autocomplete("name", "tom")

// Query member by id
val filterById = Filters.eq("id", "tommaso")

// Query multiple members by id
val filterByIds = Filters.`in`("id", listOf("tommaso", "thierry"))

// Query channel moderators
val filterByModerator = Filters.eq("is_moderator", true)

// Query for banned members in channel
val filterByBannedMembers = Filters.eq("banned", true)

// Query members with pending invites
val filterByPendingInvite = Filters.eq("invite", "pending")

// Query all the members
val filterByNone = FilterObject()

// Results can also be ordered with the _QuerySort_ param
// For example, this will order results by member creation time, descending
val createdAtDescendingSort = QuerySort<Member>().desc("created_at")
```


### Adding Members to a Channel

Using the `addMembers()` method adds the given users as members:

```kotlin
val channelClient = client.channel("messaging", "general")

// Add members with ids "thierry" and "josh"
channelClient.addMembers("thierry", "josh").enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}
```

### Removing Members from a Channel

Using the `removeMembers()` method removes the given users from members:

```kotlin
val channelClient = client.channel("messaging", "general")

// Remove member with id "tommaso"
channelClient.removeMembers("tommaso").enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}
```

## Channel Invites

_Stream Chat_ provides the ability to invite users to a channel via the `channel` method with the `invites` array. Upon invitation, the end-user will receive a notification that they were invited to the specified channel.

### Inviting a User

See the following for an example on how to invite a user by adding an `invites` array containing the user ID:

```kotlin
val channelClient = client.channel("messaging", "general") 
val data = mapOf( 
    "members" to listOf("thierry", "tommaso"), 
    "invites" to listOf("nick") 
) 
 
channelClient.create(data).enqueue { result -> 
    if (result.isSuccess) { 
        val channel = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

### Accepting an Invite

In order to accept an invite, you must use call the `acceptInvite` method. The `acceptInvite` method accepts and object with an optional message property. Please see below for an example of how to call `acceptInvite`:

```kotlin
channelClient.acceptInvite( 
    message = "Nick joined this channel!" 
).enqueue { result -> 
    if (result.isSuccess) { 
        val channel = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

### Rejecting an Invite

To reject an invite, call the `rejectInvite` method. This method does not require a user ID as it pulls the user ID from the current session in store from the `connectUser` call.

```kotlin
channelClient.rejectInvite().enqueue { result ->  
    if (result.isSuccess) {  
        // Invite rejected  
    } else {  
        // Handle result.error()  
    }  
}
```

## Querying for Accepted Invites

Querying for accepted invites is done via the `queryChannels` method. This allows you to return a list of accepted invites with a single call. See below for an example:

```kotlin
val request = QueryChannelsRequest( 
    filter = Filters.eq("invite", "accepted"), 
    offset = 0, 
    limit = 10 
) 
client.queryChannels(request).enqueue { result -> 
    if (result.isSuccess) { 
        val channels: List<Channel> = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

### Querying for Rejected Invites

Similar to querying for accepted invites, you can query for rejected invites with `queryChannels`. See below for an example:

```kotlin
val request = QueryChannelsRequest( 
    filter = Filters.eq("invite", "rejected"), 
    offset = 0, 
    limit = 10 
) 
client.queryChannels(request).enqueue { result -> 
    if (result.isSuccess) { 
        val channels: List<Channel> = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```


## Muting Channels

Messages added to a channel will not trigger push notifications, nor change the unread count for the users that muted it.

By default, mutes stay in place indefinitely until the user removes it; however, you can optionally set an expiration time.

### Muting a Channel

```kotlin
client.muteChannel(channelType, channelId)
    .enqueue { result: Result<Unit> ->
        if (result.isSuccess) {
            //channel is muted
        } else {
            result.error().printStackTrace()
        }
    }
```

### Retrieving Muted Channels

The list of muted channels and their expiration time is returned when the user connects.

```kotlin
// get list of muted channels when user is connected
client.connectUser(user, "user-token", object : InitConnectionListener() {
    override fun onSuccess(data: ConnectionData) {
        val user = data.user
        // mutes contains the list of channel mutes
        val mutes: List<ChannelMute> = user.channelMutes
    }
})

// get updates about muted channels
client
    .events()
    .subscribe { event: ChatEvent? ->
        if (event is NotificationChannelMutesUpdated) {
            val mutes = event.me.channelMutes
        } else if (event is NotificationMutesUpdated) {
            val mutes = event.me.channelMutes
        }
    }
```

:::note
Messages added to muted channels do not increase the unread messages count.
:::

Muted channels can be filtered or excluded by using the `muted` in your query channels filter.

```kotlin
// Filter for all channels excluding muted ones
val notMutedFilter = Filters.and(
    Filters.eq("muted", false),
    Filters.`in`("members", listOf(currentUserId)),
)

// Filter for muted channels
val mutedFilter = Filters.eq("muted", true)

// Executing a channels query with either of the filters
client.queryChannels(QueryChannelsRequest(
    filter = filter, // Set the correct filter here
    offset = 0,
    limit = 10,
)).enqueue { result ->
    if (result.isSuccess) {
        val channels: List<Channel> = result.data()
    } else {
        // Handle result.error()
    }
}
```

### Unmuting a Channel

```kotlin
// Unmute channel for current user
channelClient.unmute().enqueue { result ->
    if (result.isSuccess) {
        // Channel is unmuted
    } else {
        // Handle result.error()
    }
}
```


## Deleting and Hiding Channels

### Deleting a Channel

You can delete a Channel using the `delete` method. This marks the channel as deleted and hides all the content.

```kotlin
val channelClient = client.channel("messaging", "general")

channelClient.delete().enqueue { result ->
    if (result.isSuccess) {
        val channel = result.data()
    } else {
        // Handle result.error()
    }
}
```

### Hiding a Channel

Hiding a channel will remove it from query channel requests for that user until a new message is added. Please keep in mind that hiding a channel is only available to members of that channel.

Optionally you can also clear the entire message history of that channel for the user. This way, when a new message is received, it will be the only one present in the channel.

```kotlin
// Hides the channel until a new message is added there
channelClient.hide().enqueue { result ->
    if (result.isSuccess) {
        // Channel is hidden
    } else {
        // Handle result.error()
    }
}

// Shows a previously hidden channel
channelClient.show().enqueue { result ->
    if (result.isSuccess) {
        // Channel is shown
    } else {
        // Handle result.error()
    }
}

// Hide the channel and clear the message history
channelClient.hide(clearHistory = true).enqueue { result ->
    if (result.isSuccess) {
        // Channel is hidden
    } else {
        // Handle result.error()
    }
}
```

### Truncating a Channel

Messages from a channel can be truncated. This removes all of the messages but doesn't affect the channel data or members.

```kotlin
// Truncate the channel
channelClient.truncate().enqueue { result ->
    if (result.isSuccess) {
        // Channel is truncated
    } else {
        // Handle result.error()
    }
}
```

## Slow Mode

Slow mode helps reduce noise on a channel by limiting users to a maximum of 1 message per cooldown interval.

### Enabling Slow Mode

Slow mode is disabled by default and can be enabled/disabled by admins and moderators.

```kotlin
val channelClient = client.channel("messaging", "general")

// Enable slow mode and set cooldown to 1s
channelClient.enableSlowMode(cooldownTimeInSeconds = 1).enqueue { /* Result handling */ }

// Increase cooldown to 30s
channelClient.enableSlowMode(cooldownTimeInSeconds = 30).enqueue { /* Result handling */ }

// Disable slow mode
channelClient.disableSlowMode().enqueue { /* Result handling */ }
```

When a user posts a message during the cooldown period, the API returns an error message.

### Retrieving Cooldown Value

You can avoid hitting the APIs and instead show such limitation on the send message UI directly. When slow mode is enabled, channels include a cooldown field containing the current `cooldown` period in seconds.

```kotlin
val channelClient = client.channel("messaging", "general")

// Get the cooldown value
channelClient.query(QueryChannelRequest()).enqueue { result ->
    if (result.isSuccess) {
        val channel = result.data()
        val cooldown = channel.cooldown

        val message = Message(text = "Hello")
        channelClient.sendMessage(message).enqueue {
            // After sending a message, block the UI temporarily
            // The disable/enable UI methods have to be implemented by you
            disableMessageSendingUi()

            Handler(Looper.getMainLooper())
                .postDelayed(::enableMessageSendingUi, cooldown.toLong())
        }
    }
}
```

