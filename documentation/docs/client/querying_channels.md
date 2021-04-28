---
id: clientSdkQueryingChannels
title: Querying Channels
sidebar_position: 7
---

#### Querying Channel List

You can query channels based on built-in fields as well as any custom field you add to channels. Multiple filters can be combined using AND, OR logical operators, each filter can use its comparison (equality, inequality, greater than, greater or equal, etc.). You can find the complete list of supported operators in the query syntax section of the docs.

As an example, let's say that you want to query the last conversations I participated in sorted by `last_message_at`.

Here’s an example of how you can query the list of channels:

```kotlin
val request = QueryChannelsRequest(
    filter = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf("thierry")),
    ),
    offset = 0,
    limit = 10,
    querySort = QuerySort.desc("last_message_at")
).apply {
    watch = true
    state = true
}

client.queryChannels(request).enqueue { result ->
    if (result.isSuccess) {
        val channels: List<Channel> = result.data()
    } else {
        // Handle result.error()
    }
}
```

> At a minimum, the filter should include members: { $in: [userID] }.

On messaging and team applications you normally have users added to channels as a member. A good starting point is to use this filter to show the channels the user is participating.

```kotlin
val filter = Filters.`in`("members", listOf("thierry"))
```

On a support chat, you probably want to attach additional information to channels such as the support agent handling the case and other information regarding the status of the support case (ie. open, pending, solved).

```kotlin
val filter = Filters.and(
    Filters.eq("agent_id", user.id),
    Filters.`in`("status", listOf("pending", "open", "new")),
)
```

#### Paginating Channels

Query channel requests can be paginated similar to how you paginate on other calls. Here's a short example:

```kotlin
// Get the first 10 channels
val filter = Filters.`in`("members", "thierry")
val offset = 0
val limit = 10
val request = QueryChannelsRequest(filter, offset, limit)
client.queryChannels(request).enqueue { result ->
    if (result.isSuccess) {
        val channels = result.data()
    } else {
        // Handle result.error()
    }
}

// Get the second 10 channels
val nextRequest = QueryChannelsRequest(
    filter = filter,
    offset = 10, // Skips first 10
    limit = limit
)
client.queryChannels(nextRequest).enqueue { result ->
    if (result.isSuccess) {
        val channels = result.data()
    } else {
        // Handle result.error()
    }
}
```
