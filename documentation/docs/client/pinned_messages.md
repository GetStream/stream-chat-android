---
id: clientPinnedMessages
title: Pinned Messages
sidebar_position: 22
---

Pinned messages allow users to highlight important messages, make announcements, or temporarily promote content. Pinning a message is, by default, restricted to certain user roles, but this is flexible. Each channel can have multiple pinned messages and these can be created or updated with or without an expiration.

## Pinning a Message

An existing message can be updated to be pinned or unpinned by using the `channel.pinMessage` and `channel.unpinMessage` methods. Or a new message can be pinned when it is sent by setting the pinned and `pin_expires` fields when using `channel.sendMessage`.

```kotlin
// Create pinned message 
val pinExpirationDate = Calendar.getInstance().apply { set(2077, 1, 1) }.time 
val message = Message( 
    text = "Hey punk", 
    pinned = true, 
    pinExpires = pinExpirationDate 
) 
 
channelClient.sendMessage(message).enqueue { /* ... */ } 
 
// Unpin message 
channelClient.unpinMessage(message).enqueue { /* ... */ } 
 
// Pin message for 120 seconds 
channelClient.pinMessage(message, timeout = 120).enqueue { /* ... */ } 
 
// Change message expiration to 2077 
channelClient.pinMessage(message, expirationDate = pinExpirationDate).enqueue { /* ... */ } 
 
// Remove expiration date from pinned message 
channelClient.pinMessage(message, expirationDate = null).enqueue { /* ... */ }
```

| Name | Type | Description | Default | Optional |
| :--- | :--- | :--- | :--- | :--- |
| pinned | Boolean | Indicates whether the message is pinned or not | false | &check; |
| pinnedAt | Date | Date when the message got pinned | - | &check; |
| pinExpires | Date | Date when the message pin expires. An empty value means that message does not expire | null | &check; |

> To pin the message user has to have `PinMessage` permission.

## Retrieving Pinned Messages

You can easily retrieve the last 10 pinned messages from the `channel.pinned_messages` field:

```kotlin
channelClient.query(QueryChannelRequest()).enqueue { result -> 
    if (result.isSuccess) { 
        val pinnedMessages: List<Message> = result.data().pinnedMessages 
    } else { 
        // Handle result.error() 
    } 
}
```

> To learn more about channels you can visit [Querying Channels](/docs/client/clientSdkQueryingChannels)

## Searching for Pinned Messages

Stream Chat also provides a search filter in case if you need to display more than 10 pinned messages in a specific channel.

```kotlin 
val request = SearchMessagesRequest( 
    offset = 0, 
    limit = 30, 
    channelFilter = Filters.`in`("cid", "channelType:channelId"), 
    messageFilter = Filters.eq("pinned", true) 
) 
 
client.searchMessages(request).enqueue { result -> 
    if (result.isSuccess) { 
        val pinnedMessages = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```
