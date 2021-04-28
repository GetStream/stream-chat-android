---
id: clientWatchingChannel
title: Watching a Channel
sidebar_position: 8
---


The call to `channel.watch` does a few different things in one API call:

- It creates the channel if it doesn't exist yet (if this user has the right permissions to create a channel)
- It queries the channel state and returns members, watchers and messages
- It watches the channel state and tells the server that you want to receive events when anything in this channel changes

## Watching a Channel and Receiving Events

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
> **NOTE:** Watching a channel only works if you have connected as a user to the chat API

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

## Stop Receiving Channel Events

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

## Getting the Watcher Count

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

## Listening to Watching Events

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
