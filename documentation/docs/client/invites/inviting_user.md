---
id: Invites
title: Inviting a user
sidebar_position: 1
---
Stream Chat provides the ability to invite users to a channel via the `channel` method with the `invites` array. Upon invitation, the end-user will receive a notification that they were invited to the specified channel.

#### Inviting a User

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
