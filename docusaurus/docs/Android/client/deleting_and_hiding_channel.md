---
id: client-deleting-and-hiding-channel
title: Deleting and Hiding a Channel
sidebar_position: 13
---

## Deleting a Channel

You can delete a Channel using the `delete` method. This marks the channel as deleted and hides all the content.

```kotlin
val channelClient = client.channel("messaging", "general")

channelClient.delete().enqueue { result ->
    if (result.isSuccess) {
        val channel = result.data()
    } else {
        // Handle result.error()
    }
}
```

## Hiding a Channel

Hiding a channel will remove it from query channel requests for that user until a new message is added. Please keep in mind that hiding a channel is only available to members of that channel.

Optionally you can also clear the entire message history of that channel for the user. This way, when a new message is received, it will be the only one present in the channel.

```kotlin
// Hides the channel until a new message is added there
channelClient.hide().enqueue { result ->
    if (result.isSuccess) {
        // Channel is hidden
    } else {
        // Handle result.error()
    }
}

// Shows a previously hidden channel
channelClient.show().enqueue { result ->
    if (result.isSuccess) {
        // Channel is shown
    } else {
        // Handle result.error()
    }
}

// Hide the channel and clear the message history
channelClient.hide(clearHistory = true).enqueue { result ->
    if (result.isSuccess) {
        // Channel is hidden
    } else {
        // Handle result.error()
    }
}
```

## Truncating a Channel

Messages from a channel can be truncated. This removes all of the messages but doesn't affect the channel data or members.

```kotlin
// Truncate the channel
channelClient.truncate().enqueue { result ->
    if (result.isSuccess) {
        // Channel is truncated
    } else {
        // Handle result.error()
    }
}
```
