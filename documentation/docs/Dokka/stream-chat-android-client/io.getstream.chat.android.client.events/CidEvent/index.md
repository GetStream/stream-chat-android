---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[CidEvent](index.md)  
  
  
  
# CidEvent  
sealed class [CidEvent](index.md) : [ChatEvent](../ChatEvent/index.md)  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/CidEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/CidEvent/channelId/#/PointingToDeclaration/"></a>abstract val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/CidEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/CidEvent/channelType/#/PointingToDeclaration/"></a>abstract val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/CidEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/CidEvent/cid/#/PointingToDeclaration/"></a>abstract val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
  
  
## Inherited properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/CidEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](index.md#455244493%2FProperties%2F-423410878)| <a name="io.getstream.chat.android.client.events/CidEvent/createdAt/#/PointingToDeclaration/"></a>abstract val [createdAt](index.md#455244493%2FProperties%2F-423410878): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/CidEvent/type/#/PointingToDeclaration/"></a>[type](index.md#-1668725000%2FProperties%2F-423410878)| <a name="io.getstream.chat.android.client.events/CidEvent/type/#/PointingToDeclaration/"></a>abstract val [type](index.md#-1668725000%2FProperties%2F-423410878): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
  
  
## Inheritors  
  
|  Name | 
|---|
| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent///PointingToDeclaration/"></a>[ChannelDeletedEvent](../ChannelDeletedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent///PointingToDeclaration/"></a>[ChannelHiddenEvent](../ChannelHiddenEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ChannelTruncatedEvent///PointingToDeclaration/"></a>[ChannelTruncatedEvent](../ChannelTruncatedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent///PointingToDeclaration/"></a>[ChannelUpdatedEvent](../ChannelUpdatedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedByUserEvent///PointingToDeclaration/"></a>[ChannelUpdatedByUserEvent](../ChannelUpdatedByUserEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent///PointingToDeclaration/"></a>[ChannelVisibleEvent](../ChannelVisibleEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/MemberAddedEvent///PointingToDeclaration/"></a>[MemberAddedEvent](../MemberAddedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/MemberRemovedEvent///PointingToDeclaration/"></a>[MemberRemovedEvent](../MemberRemovedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/MemberUpdatedEvent///PointingToDeclaration/"></a>[MemberUpdatedEvent](../MemberUpdatedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent///PointingToDeclaration/"></a>[MessageDeletedEvent](../MessageDeletedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/MessageReadEvent///PointingToDeclaration/"></a>[MessageReadEvent](../MessageReadEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent///PointingToDeclaration/"></a>[MessageUpdatedEvent](../MessageUpdatedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent///PointingToDeclaration/"></a>[NewMessageEvent](../NewMessageEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent///PointingToDeclaration/"></a>[NotificationAddedToChannelEvent](../NotificationAddedToChannelEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent///PointingToDeclaration/"></a>[NotificationChannelDeletedEvent](../NotificationChannelDeletedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationChannelTruncatedEvent///PointingToDeclaration/"></a>[NotificationChannelTruncatedEvent](../NotificationChannelTruncatedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationInviteAcceptedEvent///PointingToDeclaration/"></a>[NotificationInviteAcceptedEvent](../NotificationInviteAcceptedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationInviteRejectedEvent///PointingToDeclaration/"></a>[NotificationInviteRejectedEvent](../NotificationInviteRejectedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent///PointingToDeclaration/"></a>[NotificationInvitedEvent](../NotificationInvitedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent///PointingToDeclaration/"></a>[NotificationMarkReadEvent](../NotificationMarkReadEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent///PointingToDeclaration/"></a>[NotificationMessageNewEvent](../NotificationMessageNewEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent///PointingToDeclaration/"></a>[NotificationRemovedFromChannelEvent](../NotificationRemovedFromChannelEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ReactionDeletedEvent///PointingToDeclaration/"></a>[ReactionDeletedEvent](../ReactionDeletedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent///PointingToDeclaration/"></a>[ReactionNewEvent](../ReactionNewEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ReactionUpdateEvent///PointingToDeclaration/"></a>[ReactionUpdateEvent](../ReactionUpdateEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/TypingStartEvent///PointingToDeclaration/"></a>[TypingStartEvent](../TypingStartEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/TypingStopEvent///PointingToDeclaration/"></a>[TypingStopEvent](../TypingStopEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ChannelUserBannedEvent///PointingToDeclaration/"></a>[ChannelUserBannedEvent](../ChannelUserBannedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent///PointingToDeclaration/"></a>[UserStartWatchingEvent](../UserStartWatchingEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent///PointingToDeclaration/"></a>[UserStopWatchingEvent](../UserStopWatchingEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/ChannelUserUnbannedEvent///PointingToDeclaration/"></a>[ChannelUserUnbannedEvent](../ChannelUserUnbannedEvent/index.md)|

