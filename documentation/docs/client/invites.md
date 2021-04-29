---
id: chanelInvites
title: Channel Invites
sidebar_position: 11
---
Stream Chat provides the ability to invite users to a channel via the `channel` method with the `invites` array. Upon invitation, the end-user will receive a notification that they were invited to the specified channel.

## Inviting a User

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

## Accepting an Invite
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

## Rejecting an Invite
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

## Querying for Rejected Invites
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

