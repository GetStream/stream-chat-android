---
id: clientSdkQueryingChannels
title: Querying Channels
sidebar_position: 7
---

## Querying Channel List

You can query channels based on built-in fields as well as any custom field you add to channels. Multiple filters can be combined using AND, OR logical operators, each filter can use its comparison (equality, inequality, greater than, greater or equal, etc.). You can find the complete list of supported operators in the [query syntax section](https://getstream.io/chat/docs/react/query_syntax/) of the docs.

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

## Paginating Channels

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

## Paginating Channel Messages

The channel query endpoint allows you to paginate the list of messages, watchers, and members for one channel. To make sure that you can retrieve a consistent list of messages, pagination does not work with simple offset/limit parameters but instead, it relies on passing the ID of the messages from the previous page.

For example: say that you fetched the first 100 messages from a channel and want to lead the next 100. To do this you need to make a channel query request and pass the ID of the oldest message if you are paginating in descending order or the ID of the newest message if paginating in ascending order.

Use the `id_lt` parameter to retrieve messages older than the provided ID and `id_gt` to retrieve messages newer than the provided ID.

The terms `id_lt` and `id_gt` stand for ID less than and ID greater than.

ID-based pagination improves performance and prevents issues related to the list of messages changing while you’re paginating. If needed, you can also use the inclusive versions of those two parameters: `id_lte` and `id_gte`.

```kotlin
val channelClient = client.channel("messaging", "general")
val pageSize = 10

// Request for the first page
val request = QueryChannelRequest()
    .withMessages(pageSize)

channelClient.query(request).enqueue { result ->
    if (result.isSuccess) {
        val messages: List<Message> = result.data().messages
        if (messages.size < pageSize) {
            // All messages loaded
        } else {
            // Load next page
            val nextRequest = QueryChannelRequest()
                .withMessages(LESS_THAN, messages.last().id, pageSize)
            // ...
        }
    } else {
        // Handle result.error()
    }
}
```

For members and watchers, we use limit and offset parameters.

> The maximum number of messages that can be retrieved at once from the API is 300.
