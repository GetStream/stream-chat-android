---
id: clientThreadAndReplies
title: Threads and Replies
sidebar_position: 19
---

Threads and replies provide your users with a way to go into more detail about a specific topic. This can be very helpful to keep the conversation organized and reduce noise.

## Creating a Thread

To create a thread you simply send a message with a `parent_id`. Have a look at the example below:

```kotlin
val message = Message( 
    text = "Hello there!", 
    parentId = parentMessage.id, 
) 
 
// Send the message to the channel 
channelClient.sendMessage(message).enqueue { result -> 
    if (result.isSuccess) { 
        val sentMessage = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

> If you specify `show_in_channel`, the message will be visible both in a thread of replies as well as the main channel.

Messages inside a thread can also have reactions, attachments and mention as any other message.

## Retrieving Thread Messages

When you read a channel you do not receive messages inside threads. The parent message includes the count of replies which it is usually what apps show as the link to the thread screen. Reading a thread and paginating its messages works in a very similar way as paginating a channel.

```
// Retrieve the first 20 messages inside the thread 
client.getReplies(parentMessage.id, limit = 20).enqueue { result -> 
    if (result.isSuccess) { 
        val replies: List<Message> = result.data() 
    } else { 
        // Handle result.error() 
    } 
} 
 
// Retrieve the 20 more messages before the message with id "42" 
client.getRepliesMore( 
    messageId = parentMessage.id, 
    firstId = "42", 
    limit = 20, 
).enqueue { /* ... */ }
```

## Quoting a Message

Instead of replying in a thread, it's also possible to quote a message. Quoting a message doesn't result in the creation of a thread; the message is quoted inline.

To quote a message, simply provide the `quoted_message_id` field when sending a message:

```kotlin
val message = Message( 
    text = "This message quotes another message!", 
    replyMessageId = originalMessage.id, 
) 
channelClient.sendMessage(message).enqueue { /* ... */ }
```

Based on the provided `quoted_message_id`, the `quoted_message` field is automatically enriched when querying channels with messages. Example response:

```xml
{ 
    "id": "message_with_quoted_message", 
    "text": "This is the first message that quotes another message", 
    "quoted_message_id": "first_message_id", 
    "quoted_message": {  
        "id": "first_message_id",  
        "text": "The initial message" 
    } 
}
```
