---
id: clientCreatingChannels
title: Creating Channels
sidebar_position: 6
---

Both channel `channel.query` and `channel.watch` methods ensure that a channel exists and create one otherwise. If all you need is to ensure that a channel exists, you can use `channel.create`.

#### Creating a Channel Using a Channel Id

```kotlin
val channelClient = client.channel(channelType = "messaging", channelId = "general")

channelClient.create().enqueue { result ->
    if (result.isSuccess) {
        val newChannel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}
```
#### Creating a Channel for a List of Members

Channels can be used to conversations between users. In most cases, you want conversations to be unique and make sure that a group of users have only a channel.

You can achieve this by leaving the channel ID empty and provide channel type and members. When you do so, the API will ensure that only one channel for the members you specified exists (the order of the members does not matter).

> You cannot add/remove members for channels created this way.

```kotlin
client.createChannel(
    channelType = "messaging",
    members = listOf("thierry", "tomasso")
).enqueue { result ->
    if (result.isSuccess) {
        val channel = result.data()
    } else {
        // Handle result.error()
    }
}
```
