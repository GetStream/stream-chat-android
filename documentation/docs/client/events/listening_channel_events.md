---
id: listeningChannelEvents
title: Listening for Channel Events
sidebar_position: 1
---
As soon as you call `ChannelClient::watch` or `ChatClient::queryChannels` you’ll start listening to these events. You can hook into specific events:

```kotlin
val channelClient = client.channel("messaging", "channelId") 
 
// Subscribe for new message events 
val disposable: Disposable = channelClient.subscribeFor<NewMessageEvent> { newMessageEvent -> 
    val message = newMessageEvent.message 
} 
 
// Dispose when you want to stop receiving events 
disposable.dispose()
```

You can also listen to all events at once:

```kotlin
val disposable: Disposable = channelClient.subscribe { event: ChatEvent -> 
    when (event) { 
        // Check for specific event types 
        is NewMessageEvent -> { 
            val message = event.message 
        } 
    } 
} 
 
// Dispose when you want to stop receiving events 
disposable.dispose()
```
