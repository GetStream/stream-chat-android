---
id: clientMutingChannels
title: Muting Channels
sidebar_position: 12
---

Messages added to a channel will not trigger push notifications, nor change the unread count for the users that muted it.

By default, mutes stay in place indefinitely until the user removes it; however, you can optionally set an expiration time.

## Muting a Channel

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

## Retrieving Muted Channels

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

> Messages added to muted channels do not increase the unread messages count.

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

## Unmuting a Channel

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
