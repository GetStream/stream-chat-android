---
id: clientSlowMode
title: Slow mode
sidebar_position: 14
---

Slow mode helps reduce noise on a channel by limiting users to a maximum of 1 message per cooldown interval.

#### Enabling Slow Mode

Slow mode is disabled by default and can be enabled/disabled by admins and moderators.

```kotlin
val channelClient = client.channel("messaging", "general")

// Enable slow mode and set cooldown to 1s
channelClient.enableSlowMode(cooldownTimeInSeconds = 1).enqueue { /* Result handling */ }

// Increase cooldown to 30s
channelClient.enableSlowMode(cooldownTimeInSeconds = 30).enqueue { /* Result handling */ }

// Disable slow mode
channelClient.disableSlowMode().enqueue { /* Result handling */ }
```

When a user posts a message during the cooldown period, the API returns an error message.

#### Retrieving Cooldown Value

You can avoid hitting the APIs and instead show such limitation on the send message UI directly. When slow mode is enabled, channels include a cooldown field containing the current `cooldown` period in seconds.

```kotlin
val channelClient = client.channel("messaging", "general")

// Get the cooldown value
channelClient.query(QueryChannelRequest()).enqueue { result ->
    if (result.isSuccess) {
        val channel = result.data()
        val cooldown = channel.cooldown

        val message = Message(text = "Hello")
        channelClient.sendMessage(message).enqueue {
            // After sending a message, block the UI temporarily
            // The disable/enable UI methods have to be implemented by you
            disableMessageSendingUi()

            Handler(Looper.getMainLooper())
                .postDelayed(::enableMessageSendingUi, cooldown.toLong())
        }
    }
}
```
