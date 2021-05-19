---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[HasUnreadCounts](index.md)  
  
  
  
# HasUnreadCounts  
interface [HasUnreadCounts](index.md)Interface that marks a [ChatEvent](../ChatEvent/index.md) as having the information about unread counts. There are certain cases when the server omits these fields (e.g. when ReadEvents option is disabled, when the number of watchers is over 100, etc). In that case totalUnreadCount and unreadChannels fields have 0 values.The list of events which contain unread counts:<ul><li>message.new</li><li>notification.message_new</li><li>notification.mark_read</li><li>notification.added_to_channel</li><li>notification.channel_deleted</li><li>notification.channel_truncated</li></ul>  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/HasUnreadCounts/totalUnreadCount/#/PointingToDeclaration/"></a>[totalUnreadCount](totalUnreadCount.md)| <a name="io.getstream.chat.android.client.events/HasUnreadCounts/totalUnreadCount/#/PointingToDeclaration/"></a>abstract val [totalUnreadCount](totalUnreadCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)|
| <a name="io.getstream.chat.android.client.events/HasUnreadCounts/unreadChannels/#/PointingToDeclaration/"></a>[unreadChannels](unreadChannels.md)| <a name="io.getstream.chat.android.client.events/HasUnreadCounts/unreadChannels/#/PointingToDeclaration/"></a>abstract val [unreadChannels](unreadChannels.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)|
  
  
## Inheritors  
  
|  Name | 
|---|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent///PointingToDeclaration/"></a>[NewMessageEvent](../NewMessageEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent///PointingToDeclaration/"></a>[NotificationAddedToChannelEvent](../NotificationAddedToChannelEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent///PointingToDeclaration/"></a>[NotificationChannelDeletedEvent](../NotificationChannelDeletedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationChannelTruncatedEvent///PointingToDeclaration/"></a>[NotificationChannelTruncatedEvent](../NotificationChannelTruncatedEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent///PointingToDeclaration/"></a>[NotificationMarkReadEvent](../NotificationMarkReadEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent///PointingToDeclaration/"></a>[MarkAllReadEvent](../MarkAllReadEvent/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent///PointingToDeclaration/"></a>[NotificationMessageNewEvent](../NotificationMessageNewEvent/index.md)|

