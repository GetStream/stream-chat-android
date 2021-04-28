---
id: clientQueryingRegularUsers
title: Querying regular users
sidebar_position: 1
---
The Query Users method allows you to search for users and see if they are online/offline. The example below shows how you can retrieve the details for 3 users in one API call:

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