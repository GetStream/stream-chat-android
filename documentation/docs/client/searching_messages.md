---
id: clientSearchingMessages
title: Searching Messages
sidebar_position: 20
---

Message search is built-in to the chat API. You can enable and/or disable the search indexing on a per-channel type. 

## Searching for Messages

The command shown below selects the channels in which John is a member. Next, it searches the messages in those channels for the keyword “'supercalifragilisticexpialidocious'”:

```kotlin
client.searchMessages( 
    SearchMessagesRequest( 
        offset = 0, 
        limit = 10, 
        channelFilter = Filters.`in`("members", listOf("john")), 
        messageFilter = Filters.autocomplete("text", "supercalifragilisticexpialidocious") 
    ) 
).enqueue { result -> 
    if (result.isSuccess) { 
        val messages: List<Message> = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

Pagination works via the standard `limit` and `offset` parameters. The first argument, `filter`, uses a MongoDB style query expression.

> We do not run MongoDB on the backend, so only a subset of the standard MongoDB filters are supported.

## Searching for Messages with Attachments

Additionally, this endpoint can be used to search for messages that have attachments.

```kotlin
channelClient.getMessagesWithAttachments( 
    offset = 0, 
    limit = 10, 
    type = "image", 
).enqueue { result -> 
    if (result.isSuccess) { 
        // These messages will contain at least one of the desired  
        // type of attachment, but not necessarily all of their  
        // attachments will have the specified type 
        val messages: List<Message> = result.data() 
    } 
}
```
