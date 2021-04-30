---
id: clientChannelMembers
title: Channel Members
sidebar_position: 10
---

## Querying Members

Sometimes channels will have many hundreds (or thousands) of members and it is important to be able to access ID's and information on all of these members. The `queryMembers` endpoint queries the channel members and allows the user to paginate through a full list of users in channels with very large member counts. The endpoint supports filtering on numerous criteria to efficiently return member information.

> The members are sorted by _created_at_ in ascending order.

> _Stream Chat_ does not run MongoDB on the backend, only a subset of the query options are available.

Here’s some example of how you can query the list of members:

```kotlin
val channelClient = client.channel("messaging", "general")

val offset = 0 // Use this value for pagination
val limit = 10
val sort = QuerySort<Member>()

// Channel members can be queried with various filters
// 1. Create the filter, e.g query members by user name
val filterByName = Filters.eq("name", "tommaso")
// 2. Call queryMembers with that filter
channelClient.queryMembers(offset, limit, filterByName, sort).enqueue { result ->
    if (result.isSuccess) {
        val members: List<Member> = result.data()
    } else {
        Log.e(TAG, String.format("There was an error %s", result.error()), result.error().cause)
    }
}

// Here are some other commons filters you can use:

// Autocomplete members by user name (names containing "tom")
val filterByAutoCompleteName = Filters.autocomplete("name", "tom")

// Query member by id
val filterById = Filters.eq("id", "tommaso")

// Query multiple members by id
val filterByIds = Filters.`in`("id", listOf("tommaso", "thierry"))

// Query channel moderators
val filterByModerator = Filters.eq("is_moderator", true)

// Query for banned members in channel
val filterByBannedMembers = Filters.eq("banned", true)

// Query members with pending invites
val filterByPendingInvite = Filters.eq("invite", "pending")

// Query all the members
val filterByNone = FilterObject()

// Results can also be ordered with the _QuerySort_ param
// For example, this will order results by member creation time, descending
val createdAtDescendingSort = QuerySort<Member>().desc("created_at")
```


## Adding Members to a Channel

Using the `addMembers()` method adds the given users as members:

```kotlin
val channelClient = client.channel("messaging", "general")

// Add members with ids "thierry" and "josh"
channelClient.addMembers("thierry", "josh").enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}
```

## Removing Members from a Channel

Using the `removeMembers()` method removes the given users from members:

```kotlin
val channelClient = client.channel("messaging", "general")

// Remove member with id "tommaso"
channelClient.removeMembers("tommaso").enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}
```
