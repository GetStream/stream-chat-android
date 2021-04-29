---
id: listeningConnectionEvents
title: Listening for Connection Events
sidebar_position: 3
---

Stream SDK makes sure that a connection to Stream Web Socket is kept alive at all times and that chat state is recovered when the user's internet connection comes back online. Your application can subscribe to changes to the connection using client events.

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