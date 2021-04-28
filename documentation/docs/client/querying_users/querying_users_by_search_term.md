---
id: clientQueryingUsersBySearchTerm
title: Querying users by search term
sidebar_position: 3
---
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
