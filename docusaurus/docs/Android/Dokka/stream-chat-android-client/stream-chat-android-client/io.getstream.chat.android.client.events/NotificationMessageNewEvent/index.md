---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[NotificationMessageNewEvent](index.md)



# NotificationMessageNewEvent  
 [androidJvm] data class [NotificationMessageNewEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channel**: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md), **message**: [Message](../../io.getstream.chat.android.client.models/Message/index.md), **totalUnreadCount**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **unreadChannels**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [CidEvent](../CidEvent/index.md), [HasChannel](../HasChannel/index.md), [HasMessage](../HasMessage/index.md), [HasUnreadCounts](../HasUnreadCounts/index.md)

Triggered when a message is added to a channel

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/NotificationMessageNewEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Channel#io.getstream.chat.android.client.models.Message#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[NotificationMessageNewEvent](NotificationMessageNewEvent.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/NotificationMessageNewEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Channel#io.getstream.chat.android.client.models.Message#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a> [androidJvm] fun [NotificationMessageNewEvent](NotificationMessageNewEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channel: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md), message: [Message](../../io.getstream.chat.android.client.models/Message/index.md), totalUnreadCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, unreadChannels: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0)   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/channel/#/PointingToDeclaration/"></a>[channel](channel.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/channel/#/PointingToDeclaration/"></a> [androidJvm] open override val [channel](channel.md): [Channel](../../io.getstream.chat.android.client.models/Channel/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/message/#/PointingToDeclaration/"></a>[message](message.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/message/#/PointingToDeclaration/"></a> [androidJvm] open override val [message](message.md): [Message](../../io.getstream.chat.android.client.models/Message/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/totalUnreadCount/#/PointingToDeclaration/"></a>[totalUnreadCount](totalUnreadCount.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/totalUnreadCount/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = total_unread_count)  <br/>  <br/>open override val [totalUnreadCount](totalUnreadCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/unreadChannels/#/PointingToDeclaration/"></a>[unreadChannels](unreadChannels.md)| <a name="io.getstream.chat.android.client.events/NotificationMessageNewEvent/unreadChannels/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = unread_channels)  <br/>  <br/>open override val [unreadChannels](unreadChannels.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|

