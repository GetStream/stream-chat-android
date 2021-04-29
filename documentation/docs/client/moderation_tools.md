---
id: clientModerationTools
title: Moderation Tools
sidebar_position: 23
---

## Flagging a Message or a User

Any user is allowed to flag a message or a user. Flagging does not perform any particular action on the chat. The API will only trigger the related webhook event and make the message appear on your _Dashboard Chat Moderation_ view.

```kotlin
client.flagMessage("message-id").enqueue { result -> 
    if (result.isSuccess) { 
        // Message was flagged 
        val flag: Flag = result.data() 
    } else { 
        // Handle result.error() 
    } 
} 
 
client.flagUser("user-id").enqueue { result -> 
    if (result.isSuccess) { 
        // User was flagged 
        val flag: Flag = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

## Muting a User

Any user is allowed to mute another user. Mutes are stored at user level and returned with the rest of the user information when `connectUser` is called. A user will be be muted until the user is `unmuted` or the mute is expired.

```kotlin 
client.muteUser("user-id").enqueue { result -> 
    if (result.isSuccess) { 
        // User was muted 
        val mute: Mute = result.data() 
    } else { 
        // Handle result.error() 
    } 
} 
 
client.unmuteUser("user-id").enqueue { result -> 
    if (result.isSuccess) { 
        // User was unmuted 
    } else { 
        // Handle result.error() 
    } 
}
```

After muting a user messages will still be delivered via web-socket. Implementing business logic such as hiding messages from muted users or display them differently is left to the developer to implement.

> Messages from muted users are not delivered via push (APN/Firebase)

## Banning a User

Users can be banned from an app entirely or just from a single channel. When a user is banned, it will not be allowed to post messages until the ban is removed or expired but they will be able to connect to Chat and to channels as before.

> In most cases, only admins or moderators are allowed to ban other users from a channel.

| Name | Type | Description | Default | Optional |
| :--- | :--- | :--- | :--- | :--- |
| timeout | Int | The timeout in minutes until the ban is automatically expired. | 	no limit | &check; |
| reason | String | The reason that the ban was created. | | &check; |

> Banning a user from all channels can only be done using server-side auth.

```kotlin
// Ban user for 60 minutes from a channel 
channelClient.banUser(targetId = "user-id", reason = "Bad words", timeout = 60).enqueue { result -> 
    if (result.isSuccess) { 
        // User was banned 
    } else { 
        // Handle result.error() 
    } 
} 
 
channelClient.unBanUser(targetId = "user-id").enqueue { result -> 
    if (result.isSuccess) { 
        // User was unbanned 
    } else { 
        // Handle result.error() 
    } 
}
```

## Shadow Banning a User

Users can be shadow banned from an app entirely or from a channel. When a user is shadow banned, it will still be allowed to post messages, but any message sent during, will have `shadowed: true` field. However, this will be invisible to the author of the message.

> It's up to the client-side implementation to handle `shadowed` messages appropriately.

```kotlin
// Shadow ban user for 60 minutes from a channel 
channelClient.shadowBanUser(targetId = "user-id", reason = "Bad words", timeout = 60).enqueue { result -> 
     if (result.isSuccess) { 
         // User was shadow banned 
     } else { 
         // Handle result.error() 
     } 
} 
 
channelClient.removeShadowBan("user-id").enqueue { result -> 
    if (result.isSuccess) { 
        // Shadow ban was removed 
    } else { 
        // Handle result.error() 
    } 
}
```

Administrators can view shadow banned user status in `queryChannels()`, `queryMembers()` and `queryUsers()`.

## Retrieving Banned Users

Banned users can be retrieved in different ways:
1. Using the dedicated query bans endpoint
2. User Search: you can add the `banned:true` condition to your search. Please note that this will only return users that were banned at the app-level and not the ones that were banned only in channels.

```kotlin
// retrieve the list of banned users 
client.queryUsers( 
    QueryUsersRequest( 
        filter = Filters.eq("banned", true), 
        offset = 0, 
        limit = 10, 
    ) 
).enqueue { result -> 
    if (result.isSuccess) { 
       val users: List<User> = result.data() 
   } else { 
       // Handle result.error() 
   } 
} 
 
// Query for banned members from one channel 
client.queryBannedUsers(filter = Filters.eq("channel_cid", "ChannelType:ChannelId")).enqueue { result -> 
    if (result.isSuccess) { 
        val bannedUsers: List<BannedUser> = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

## Retrieving Banned Users From Specific Channels

You can list banned users from a specific channel using the query banned users endpoint which allows you to get paginated results:

```kotlin
// Get the bans for channel livestream:123 in descending order
channelClient.queryBannedUsers(
    sort = QuerySort.desc(BannedUsersSort::createdAt),
).enqueue { result ->
    if (result.isSuccess) {
        val bannedUsers: List<BannedUser> = result.data()
    } else {
        // Handle result.error()
   }
}

// Get the page of bans which where created before or equal date for the same channel
client.queryBannedUsers(
    filter = Filters.eq("channel_cid", "livestream:123"),
    sort = QuerySort.desc(BannedUsersSort::createdAt),
    createdAtBeforeOrEqual = Date(),
).enqueue { result ->
    if (result.isSuccess) {
        val bannedUsers: List<BannedUser> = result.data()
    } else {
        // Handle result.error()
    }
}
```

You can also use `in` filter to query banned users from multiple channels:

```kotlin
client.queryBannedUsers(
    filter = Filters.`in`("channel_cid", listOf("livestream:123", "livestream:456")),
    sort = QuerySort.desc(BannedUsersSort::createdAt),
    createdAtBeforeOrEqual = Date(),
).enqueue { result ->
    if (result.isSuccess) {
        val bannedUsers: List<BannedUser> = result.data()
    } else {
        // Handle result.error()
    }
}
```
