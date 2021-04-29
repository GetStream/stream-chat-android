---
id: clientSilentMessages
title: Silent Messages
sidebar_position: 21
---

Sometimes you want to add system or transactional messages to channels such as: "your ride is waiting for you", "James updated the information for the trip", "You and Jane are now matched" and so on.

You may not want these messages to mark a channel as unread or increase the unread messages for users.

Silent messages are special messages that don't increase the unread messages count nor mark a channel as unread.

## Sending a Silent Message

Creating a silent message is very simple, you only need to include the `silent` field boolean field and set it to `true`.

```kotlin
val message = Message( 
    text = "You and Jane are now matched!", 
    user = systemUser, 
    silent = true, 
) 
channelClient.sendMessage(message).enqueue { /* ... */ }
```

> Existing messages cannot be turned into a silent message or vice versa.

> Silent messages do send push notifications by default. To skip our push notification service, mark the message with `skip_push: true`
