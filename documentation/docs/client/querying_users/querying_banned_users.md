---
id: clientQueryingBannedUsers
title: Querying banned users
sidebar_position: 2
---
Another option is to query for banned users. This can be done with the following code snippet:

```kotlin
val request = QueryUsersRequest( 
    filter = Filters.eq("banned", true), 
    offset = 0, 
    limit = 10, 
) 
 
client.queryUsers(request).enqueue { /* ... */ }
```

Please be aware that this query will return users banned across the entire app, not at a channel level.

You can filter and sort on the custom fields you've set for your user, the user id, and when the user was last active.

The options for the queryUser method are presence, limit, and offset. If presence is true this makes sure you receive the user.presence.changed event when a user goes online or offline.