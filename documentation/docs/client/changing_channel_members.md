---
id: clientUserTypes
title: Changing Channel Members
sidebar_position: 4
---

#### Adding Members to a Channel

Using the `addMembers()` method adds the given users as members:

```kotlin
val channelClient = client.channel("messaging", "general")

// Add members with ids "thierry" and "josh"
channelClient.addMembers("thierry", "josh").enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}
```

#### Removing Members from a Channel

Using the `removeMembers()` method removes the given users from members:

```kotlin
val channelClient = client.channel("messaging", "general")

// Remove member with id "tommaso"
channelClient.removeMembers("tommaso").enqueue { result ->
    if (result.isSuccess) {
        val channel: Channel = result.data()
    } else {
        // Handle result.error()
    }
}
```
