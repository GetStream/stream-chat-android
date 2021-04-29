---
id: AcceptingInvite
title: Accepting an invite
sidebar_position: 2
---
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
