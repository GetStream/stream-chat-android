# Messages

<!-- TODO: Add brief intro about what messages are, what components they have (attachment, reactions, etc.) -->

## Sending Messages

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

### Sending a Message with Attachment

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

## Uploading Files

The `channel.sendImage` and `channel.sendFile` methods make it easy to upload files.

This functionality defaults to using the Stream CDN. If you would like, you can easily change the logic to upload to your own CDN of choice. The maximum file size is 20mb for the Stream Chat CDN.

<!-- TODO: Add text to the below sections so that they're not just code snippets -->

### Uploading an Image

```kotlin
val channelClient = client.channel("messaging", "general")

// Upload an image without detailed progress
channelClient.sendImage(imageFile).enqueue { result->
    if (result.isSuccess) {
        // Successful upload, you can now attach this image
        // to an message that you then send to a channel
        val imageUrl = result.data()
        val attachment = Attachment(
            type = "image",
            imageUrl = imageUrl,
        )
        val message = Message(
            attachments = mutableListOf(attachment),
        )
        channelClient.sendMessage(message).enqueue { /* ... */ }
    }
}
```

:::note
Attachments need to be linked to the message after the upload is completed.
:::

### Uploading a File

```kotlin
// Upload a file, monitoring for progress with a ProgressCallback
channelClient.sendFile(anyOtherFile, object : ProgressCallback {
    override fun onSuccess(file: String) {
        val fileUrl = file
    }

    override fun onError(error: ChatError) {
        // Handle error
    }

    override fun onProgress(progress: Long) {
        // You can render the uploading progress here
    }
}).enqueue() // No callback passed to enqueue, as we'll get notified above anyway
```

### Using Your Own CDN

The SDK allows you to use your own CDN by creating your own implementation of the `FileUploader` interface, and pass it to `ChatClient.Builder`.

The example below show how to change where files are uploaded:

```kotlin
// Create a custom FileUploader
class MyFileUploader : FileUploader {
    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): Result<String> {
        return try {
            // Send the file to your own CDN
            val url = ...
            // Return a Result object with file url
            Result.success(url)
        } catch (e: Exception) {
            // Return a Result object with exception in case upload failed
            Result.error(e)
        }
    }
    ...
}

// Set a custom FileUploader implementation when building your client
val client = ChatClient.Builder("39mr6a3z4tem", context)
    .fileUploader(MyFileUploader())
    .build()
```

## Reactions

Stream Chat has built-in support for user reactions. Common examples are likes, comments, loves, etc. Reactions can be customized so that you are able to use any type of reaction your application requires.

Similar to other objects in _Stream Chat_, reactions allow you to add custom data to the reaction of your choice. This is helpful if you want to customize the reaction logic.

### Sending a Reaction

<!-- TODO: Add written text to this section, remove unnecessary table copied from CMS -->

```kotlin
val channelClient = client.channel("messaging", "general") 
 
val reaction = Reaction( 
    messageId = "message-id", 
    type = "like", 
    score = 1,
    extraData = mutableMapOf("customField" to 1)
) 
channelClient.sendReaction(reaction).enqueue { result -> 
    if (result.isSuccess) { 
        val sentReaction: Reaction = result.data() 
    } else { 
        // Handle result.error() 
    } 
} 
```

| Name | Type | Description | Default | Optional |
| :--- | :--- | :--- | :--- | :--- |
| reaction.messageId | String | ID of the message to react to | | |
| reaction.type | String | Type of the reaction. The user can have only 1 reaction of each type per message | | |
| reaction.score | Int | Score of the reaction for cumulative reactions (see example below) | 1 | &check; |
| enforceUnique | Boolean | If set to true, new reaction will replace all reactions the user has (if any) on this message | false | &check; |

:::note
Custom data for reactions is limited to 1KB.
:::

### Replacing a Reaction

<!-- TODO: Add written text to this section -->

```kotlin
// Add reaction 'like' and replace all other reactions of this user by it
channelClient.sendReaction(reaction, enforceUnique = true).enqueue { result -> 
    if (result.isSuccess) { 
        val sentReaction = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

### Deleting a Reaction

<!-- TODO: Add written text to this section -->

```kotlin
channelClient.deleteReaction( 
    messageId = "message-id", 
    reactionType = "like", 
).enqueue { result -> 
    if (result.isSuccess) { 
        val message = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

### Paginating Reactions

Messages returned by the APIs automatically include 10 most recent reactions. You can also retrieve more reactions and paginate using the following logic:

<!-- TODO: Break up code snippets into smaller ones, with proper text inbetween -->

```kotlin
// Get the first 10 reactions 
channelClient.getReactions( 
    messageId = "message-id", 
    offset = 0, 
    limit = 10, 
).enqueue { result -> 
    if (result.isSuccess) { 
        val reactions: List<Reaction> = result.data() 
    } else { 
        // Handle result.error() 
    } 
} 
 
// Get the second 10 reactions 
channelClient.getReactions( 
    messageId = "message-id", 
    offset = 10, 
    limit = 10, 
).enqueue { /* ... */ } 
 
// Get 10 reactions after particular reaction 
channelClient.getReactions( 
    messageId = "message-id", 
    firstReactionId = "reaction-id", 
    limit = 10, 
).enqueue { /* ... */ }
```

### Cumulative (Clap) Reactions

You can use the Reactions API to build something similar to Medium's clap reactions. If you are not familiar with this, Medium allows you to clap articles more than once and shows the sum of all claps from all users.

To do this, you only need to include a score for the reaction (ie. user X clapped 25 times) and the API will return the sum of all reaction scores as well as each user individual scores (ie. clapped 475 times, user Y clapped 14 times).

```kotlin
val reaction = Reaction(messageId = "message-id", type = "clap", score = 5) 
channelClient.sendReaction(reaction).enqueue { /* ... */ }
```


## Threads and Replies

Threads and replies provide your users with a way to go into more detail about a specific topic. This can be very helpful to keep the conversation organized and reduce noise.

### Creating a Thread

To create a thread you simply send a message with a `parentId`. Have a look at the example below:

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

<!-- TODO: show_in_channel is not Android API -->

:::note
If you specify `show_in_channel`, the message will be visible both in a thread of replies as well as the main channel.
:::

Messages inside a thread can also have reactions, attachments and mentions as any other message.

### Retrieving Thread Messages

Thread messages are separated from regular messages and won't be included in regular message response. In order to get messages from a particular thread - you need to pass parent message id in messages requests.

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

### Quoting a Message

<!-- TODO: Are we calling these Replies or Quotes? Use just one, be consistent with other platforms/docs. -->

Instead of replying in a thread, it's also possible to quote a message. Quoting a message doesn't result in the creation of a thread; the message is quoted inline.

To quote a message, simply provide the `quoted_message_id` field when sending a message:

```kotlin
val message = Message( 
    text = "This message quotes another message!", 
    replyMessageId = originalMessage.id, 
) 
channelClient.sendMessage(message).enqueue { /* ... */ }
```

<!-- TODO: Backend fields and JSON again, get rid of these, only talk about Android API -->

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


## Searching Messages

Message search is built-in to the chat API. You can enable and/or disable the search indexing on a per-channel type. 

### Searching for Messages

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

<!-- TODO: Do these MongoDB mentions here make any sense at all? -->

Pagination works via the standard `limit` and `offset` parameters. The first argument, `filter`, uses a MongoDB style query expression.

:::note
We do not run MongoDB on the backend, so only a subset of the standard MongoDB filters are supported.
:::

### Searching for Messages with Attachments

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


## Silent Messages

Silent messages are special messages that don't increase the unread messages count nor mark a channel as unread. They can be used to send system or transactional messages to channels such as: "your ride is waiting for you", "James updated the information for the trip", "You and Jane are now matched" and so on.

### Sending a Silent Message

Creating a silent message is very simple, you only need to include the `silent` field boolean field and set it to `true`.

```kotlin
val message = Message( 
    text = "You and Jane are now matched!", 
    user = systemUser, 
    silent = true, 
) 
channelClient.sendMessage(message).enqueue { /* ... */ }
```

:::note
Existing messages cannot be turned into a silent message or vice versa.
:::

<!-- TODO: What's the Android API for skip_push? -->
:::note
Silent messages do send push notifications by default. To skip our push notification service, mark the message with `skip_push: true`
:::

## Pinned Messages

Pinned messages allow users to highlight important messages, make announcements, or temporarily promote content. Pinning a message is, by default, restricted to certain user roles, but this is flexible. Each channel can have multiple pinned messages and these can be created or updated with or without an expiration.

### Pinning a Message

An existing message can be updated to be pinned or unpinned by using the `ChannelClient::pinMessage` and `ChannelClient::unpinMessage` methods. 

A new message can be pinned when it is sent by setting the `pinned` and `pinExpires` properties.

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

### Retrieving Pinned Messages

You can easily retrieve the last 10 pinned messages of a `Channel` using the `pinnedMessages` property:

```kotlin
channelClient.query(QueryChannelRequest()).enqueue { result -> 
    if (result.isSuccess) { 
        val pinnedMessages: List<Message> = result.data().pinnedMessages 
    } else { 
        // Handle result.error() 
    } 
}
```

Learn more about querying channels on the [Channels](./02-channels.md) page.

### Searching for Pinned Messages

You can also use a search filter if you need to display more than 10 pinned messages in a specific channel.

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
