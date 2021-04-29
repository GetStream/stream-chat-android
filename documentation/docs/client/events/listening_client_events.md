---
id: listeningClientEvents
title: Listening for Client Events
sidebar_position: 2
---

Not all events are specific to channels. Events such as the user's status change, the users' unread count change, and other notifications are sent as client events. These events can be listened to through the `ChatClient` directly:

```kotlin
// Subscribe for User presence events 
client.subscribeFor<UserPresenceChangedEvent> { event -> 
    // Handle change 
} 
 
// Subscribe for just the first ConnectedEvent 
client.subscribeForSingle<ConnectedEvent> { event -> 
    // Use event data 
    val unreadCount = event.me.totalUnreadCount 
    val unreadChannels = event.me.unreadChannels 
}
```
