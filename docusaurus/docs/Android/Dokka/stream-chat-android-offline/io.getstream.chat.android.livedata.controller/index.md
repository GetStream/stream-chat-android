---
title: index
sidebar_position: 1
---
/[stream-chat-android-offline](../index.md)/[io.getstream.chat.android.livedata.controller](index.md)  
  
  
  
# Package io.getstream.chat.android.livedata.controller  
  
  
## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.controller/ChannelController///PointingToDeclaration/"></a>[ChannelController](ChannelController/index.md)| <a name="io.getstream.chat.android.livedata.controller/ChannelController///PointingToDeclaration/"></a>interface [ChannelController](ChannelController/index.md)The Channel Controller exposes convenient livedata objects to build your chat interface It automatically handles the incoming events and keeps users, messages, reactions, channel information up to date automatically Offline storage is also handled using RoomThe most commonly used livedata objects are<ul><li>.messages (the livedata for the list of messages)</li><li>.channelData (livedata object with the channel name, image, etc.)</li><li>.members (livedata object with the members of this channel)</li><li>.watchers (the people currently watching this channel)</li><li>.typing (who is currently typing)</li></ul>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController///PointingToDeclaration/"></a>[QueryChannelsController](QueryChannelsController/index.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController///PointingToDeclaration/"></a>interface [QueryChannelsController](QueryChannelsController/index.md)The QueryChannelsController is a small helper to show a list of channels<ul><li>.channels a livedata object with the list of channels. this list</li><li>.loading if we're currently loading</li><li>.loadingMore if we're currently loading more channels</li></ul>|
| <a name="io.getstream.chat.android.livedata.controller/ThreadController///PointingToDeclaration/"></a>[ThreadController](ThreadController/index.md)| <a name="io.getstream.chat.android.livedata.controller/ThreadController///PointingToDeclaration/"></a>interface [ThreadController](ThreadController/index.md)The threadController exposes livedata for a thread<ul><li>threadId (the id of the current thread)</li><li>loadingOlderMessages (if we're currently loading older messages)</li><li>endOfOlderMessages (if you've reached the end of older messages)</li></ul>|

