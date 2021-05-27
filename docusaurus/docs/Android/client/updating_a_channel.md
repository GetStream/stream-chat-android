---
id: client-updating-a-channel
title: Updating a Channel
sidebar_position: 9
---

There are two ways to update a channel using the Stream API - a partial or full update. A partial update will retain any custom key-value data, whereas a complete update is going to remove any that are unspecified in the API request.

## Partial Update

A partial update can be used to set and unset specific fields when it is necessary to retain additional custom data fields on the object. AKA a patch style update.

```kotlin
// Here's a channel with some custom field data that might be useful
val channelClient = client.channel(channelType = "messaging", channelId = "general")

channelClient.create(
    members = listOf("thierry", "tomasso"),
    extraData = mapOf(
        "source" to "user",
        "source_detail" to mapOf("user_id" to 123),
        "channel_detail" to mapOf(
            "topic" to "Plants and Animals",
            "rating" to "pg"
        )
    )
).execute()

// let's change the source of this channel
channelClient.updatePartial(set = mapOf("source" to "system")).execute()

// since it's system generated we no longer need source_detail
channelClient.updatePartial(unset = listOf("source_detail")).execute()

// and finally update one of the nested fields in the channel_detail
channelClient.updatePartial(set = mapOf("channel_detail.topic" to "Nature")).execute()

// and maybe we decide we no longer need a rating
channelClient.updatePartial(unset = listOf("channel_detail.rating")).execute()
```

## Full Update

The `updateChannel` function updates all of the channel data. <b>Any data that is present on the channel and not included in a full update will be deleted.</b>

```kotlin
val channelClient = client.channel("messaging", "general")

channelClient.update(
    message = Message(
        text = "Thierry changed the channel color to green"
    ),
    extraData = mapOf(
        "name" to "myspecialchannel",
        "color" to "green",
    ),
).enqueue { result ->
    if (result.isSuccess) {
        val channel = result.data()
    } else {
        // Handle result.error()
    }
}
```
