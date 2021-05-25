---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[NotificationChannelDeletedEvent](index.md)



# NotificationChannelDeletedEvent  
 [androidJvm] data class [NotificationChannelDeletedEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channel**: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md), **totalUnreadCount**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **unreadChannels**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [CidEvent](../CidEvent/index.md), [HasChannel](../HasChannel/index.md), [HasUnreadCounts](../HasUnreadCounts/index.md)

Triggered when a channel is deleted

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/NotificationChannelDeletedEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Channel#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[NotificationChannelDeletedEvent](NotificationChannelDeletedEvent.md)| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/NotificationChannelDeletedEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Channel#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a> [androidJvm] fun [NotificationChannelDeletedEvent](NotificationChannelDeletedEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channel: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md), totalUnreadCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, unreadChannels: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0)   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/channel/#/PointingToDeclaration/"></a>[channel](channel.md)| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/channel/#/PointingToDeclaration/"></a> [androidJvm] open override val [channel](channel.md): [Channel](../../io.getstream.chat.android.client.models/Channel/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/totalUnreadCount/#/PointingToDeclaration/"></a>[totalUnreadCount](totalUnreadCount.md)| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/totalUnreadCount/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = total_unread_count)  <br/>  <br/>open override val [totalUnreadCount](totalUnreadCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/unreadChannels/#/PointingToDeclaration/"></a>[unreadChannels](unreadChannels.md)| <a name="io.getstream.chat.android.client.events/NotificationChannelDeletedEvent/unreadChannels/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = unread_channels)  <br/>  <br/>open override val [unreadChannels](unreadChannels.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|

