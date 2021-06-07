# Events

## Listening for Channel Events

As soon as you call `ChannelClient::watch` or `ChatClient::queryChannels` you’ll start to listen to these events. You can hook into specific events:

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

## Listening for Client Events

Not all events are specific to channels. Events such as the user's status has changed, the users' unread count has changed, and other notifications are sent as client events. These events can be listened to through the client directly:

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

## Listening for Connection Events

The official SDKs make sure that a connection to Stream is kept alive at all times and that chat state is recovered when the user's internet connection comes back online. Your application can subscribe to changes to the connection using client events.

```kotlin
client.subscribeFor( 
    ConnectedEvent::class, 
    ConnectingEvent::class, 
    DisconnectedEvent::class, 
) { event -> 
    when (event) { 
        is ConnectedEvent -> { 
            // Socket is connected 
        } 
        is ConnectingEvent -> { 
            // Socket is connecting 
        } 
        is DisconnectedEvent -> { 
            // Socket is disconnected 
        } 
    } 
}
```

## Stop Listening for Events

It is a good practice to unregister event handlers once they are not in use anymore. Doing so will save you from performance degradations coming from memory leaks or even from errors and exceptions (i.e. null pointer exceptions)

```kotlin
val disposable: Disposable = client.subscribe { /* ... */ } 
disposable.dispose()
```
