---
id: clientTypingIndicators
title: Typing Indicators
sidebar_position: 24
---

In order to support typing events you will need to take care of four things:
1. Send `typing.start` event  when the user starts typing
2. Send `typing.stop`  event after the user stopped typing
3. Handle the two events and use them to toggle the typing indicator UI
4. Use `parent_id` field of the event to indicate that typing is happening in a thread

## Sending Typing Events

```kotlin
// Sends a typing.start event at most once every two seconds 
channelClient.keystroke().enqueue() 
 
// Sends a typing.start event for a particular thread 
channelClient.keystroke(parentId = "threadId").enqueue() 
 
// Sends the typing.stop event 
channelClient.stopTyping().enqueue()
```

When sending events on user input, you should make sure to follow some best-practices to avoid bugs.

1. Only send `typing.start` when the user starts typing
2. Send `typing.stop` after a few seconds since the last keystroke

## Observing Typing Events

```kotlin
// Add typing start event handling 
channelClient.subscribeFor<TypingStartEvent> { typingStartEvent -> 
    // Handle event 
} 
 
// Add typing stop event handling 
channelClient.subscribeFor<TypingStopEvent> { typingStopEvent -> 
    // Handle event 
}
```

> Because clients might fail at sending `typing.stop` event all you should periodically prune the list of typing users.
