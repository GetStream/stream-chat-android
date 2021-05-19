---
title: newChannelEventFilter
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.controller](../index.md)/[QueryChannelsController](index.md)/[newChannelEventFilter](newChannelEventFilter.md)  
  
  
  
# newChannelEventFilter  
abstract var [newChannelEventFilter](newChannelEventFilter.md): (Channel, FilterObject) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)When the NotificationAddedToChannelEvent is triggered the newChannelEventFilter determines if the channel should be added to the query or not. Return true to add the channel, return false to ignore it. By default it will simply add every channel for which this event is received
