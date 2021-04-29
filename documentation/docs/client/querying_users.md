---
id: clientSdkQueryingUsers
title: Querying Users
sidebar_position: 4
---

## Querying Regular Users
The `ChatClient::queryUsers` method allows you to search for users and see if they are online/offline. The example below shows how you can retrieve the details for 3 users in one API call:

```kotlin
// Search for users with id "john", "jack", or "jessie" 
val request = QueryUsersRequest( 
    filter = Filters.`in`("id", listOf("john", "jack", "jessie")), 
    offset = 0, 
    limit = 3, 
) 
 
client.queryUsers(request).enqueue { result -> 
    if (result.isSuccess) { 
        val users: List<User> = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

## Querying banned users
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

## Querying users by search term
You can autocomplete the results of your user query by username and/or ID.

If you want to return all users whose username includes 'ro', you could do so with the following:
```kotlin
val request = QueryUsersRequest( 
    filter = Filters.autocomplete("name", "ro"), 
    offset = 0, 
    limit = 10, 
) 
client.queryUsers(request).enqueue { /* ... */ }
```

This would return an array of any matching users, such as:

```kotlin
[ 
    { 
        "id": "userID", 
        "name": "Curiosity Rover" 
    }, 
    { 
        "id": "userID2", 
        "name": "Roxy" 
    }, 
    { 
        "id": "userID3", 
        "name": "Roxanne" 
    } 
]
```
