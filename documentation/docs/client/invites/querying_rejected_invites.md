---
id: QueryingRejectedInvites
title: Accepting for Rejected Invites
sidebar_position: 5
---
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
