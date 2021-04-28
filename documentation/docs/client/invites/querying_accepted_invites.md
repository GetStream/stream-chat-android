---
id: QueryingAcceptedInvites
title: Accepting for Accepted Invites
sidebar_position: 4
---
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
