---
id: clientMessages
title: Messages
sidebar_position: 15
---

## Sending a Message

You can send a simple message using the `sendMessage` call:

```kotlin
val channelClient = client.channel("messaging", "general")
val message = Message( text = "Sample message text" )

channelClient.sendMessage(message).enqueue { result ->
    if (result.isSuccess) {
        val sentMessage: Message = result.data()
    } else {
        // Handle result.error()
    }
}
```

## Sending a Message with Attachment

You can send a message with an attachment using the `sendMessage` call:

```kotlin
// Create an image attachment
val attachment = Attachment(
    type = "image",
    imageUrl = "https://bit.ly/2K74TaG",
    thumbUrl = "https://bit.ly/2Uumxti",
    extraData = mutableMapOf("myCustomField" to 123),
)

// Create a message with the attachment and a user mention
val message = Message(
    text = "@Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish.",
    attachments = mutableListOf(attachment),
    mentionedUsersIds = mutableListOf("josh-id"),
    extraData = mutableMapOf("anotherCustomField" to 234),
)

// Send the message to the channel
channelClient.sendMessage(message).enqueue { /* ... */ }
```

## Getting a Message

You can get a single message by its ID using the `getMessage` call:

```kotlin
channelClient.getMessage("message-id").enqueue { result ->
    if (result.isSuccess) {
        val message = result.data()
    } else {
        // Handle result.error()
    }
}
```

## Updating a Message

You can edit a message by calling `updateMessage` and including a message with an ID:

```kotlin
// Update some field of the message
message.text = "my updated text"

// Send the message to the channel
channelClient.updateMessage(message).enqueue { result ->
    if (result.isSuccess) {
        val updatedMessage = result.data()
    } else {
        // Handle result.error()
    }
}
```

## Deleting a Message

You can delete a message by calling `deleteMessage` and including a message with an ID:

```kotlin
channelClient.deleteMessage("message-id").enqueue { result ->
    if (result.isSuccess) {
        val deletedMessage = result.data()
    } else {
        // Handle result.error()
    }
}
```
