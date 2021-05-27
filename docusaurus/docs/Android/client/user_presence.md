---
id: client-user-presence
title: User Presence
sidebar_position: 5
---

User presence allows you to show when a user was last active and if they are online right now. Whenever you read a user the data will look like this:

```xml
{
    id: 'unique_user_id',
    online: true,
    status: 'Eating a veggie burger...',
    last_active: '2019-01-07T13:17:42.375Z'
}
```
> The online field indicates if the user is online. The status field stores text indicating the current user status.

## Marking a User Invisible

To mark a user invisible simply set the `invisible` property to `true`. You can also set a custom status message at the same time:

```kotlin
val user = User(
    id = "user-id",
    invisible = true,
)
client.connectUser(user, "user-token").enqueue { result ->
    if (result.isSuccess) {
        val user: ConnectionData = result.data()
    } else {
        // Handle result.error()
    }
}
```
> When invisible is set to true, the current user will appear as offline to other users.

> **NOTE**: User's invisible status can be only set while calling `connectUser` method

## Listening to User Presence Changes

These 3 endpoints allow you to watch user presence:

```kotlin
// You need to be watching some channels/queries to be able to get presence events.
// Here are three different ways of doing that:

// 1. Watch a single channel with presence = true set
val watchRequest = WatchChannelRequest().apply {
    data["members"] = listOf("john", "jack")
    presence = true
}
channelClient.watch(watchRequest).enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}

// 2. Query some channels with presence = true set
val channelsRequest = QueryChannelsRequest(
    filter = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf("john", "jack")),
    ),
    offset = 0,
    limit = 10,
).apply {
    presence = true
}
client.queryChannels(channelsRequest).enqueue { result ->
    if (result.isSuccess) {
        val channels: List<Channel> = result.data()
    } else {
        // Handle result.error()
    }
}

// 3. Query some users with presence = true set
val usersQuery = QueryUsersRequest(
    filter = Filters.`in`("id", listOf("john", "jack")),
    offset = 0,
    limit = 2,
    presence = true,
)
client.queryUsers(usersQuery).enqueue { result ->
    if (result.isSuccess) {
        val users: List<User> = result.data()
    } else {
        // Handle result.error()
    }
}

// Finally, subscribe to presence to events
client.subscribeFor<UserPresenceChangedEvent> { event ->
    // Handle change
}
```

Users' online status change can be handled via event delegation by subscribing to the `user.presence.changed` event the same you do for any other event.
