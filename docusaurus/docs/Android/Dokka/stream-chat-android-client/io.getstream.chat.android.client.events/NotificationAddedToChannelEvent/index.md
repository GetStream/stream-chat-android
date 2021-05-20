---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[NotificationAddedToChannelEvent](index.md)  
  
  
  
# NotificationAddedToChannelEvent  
data class [NotificationAddedToChannelEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channel**: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md), **totalUnreadCount**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **unreadChannels**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [CidEvent](../CidEvent/index.md), [HasChannel](../HasChannel/index.md), [HasUnreadCounts](../HasUnreadCounts/index.md)Triggered when the user is added to the list of channel members  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/NotificationAddedToChannelEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Channel#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[NotificationAddedToChannelEvent](NotificationAddedToChannelEvent.md)| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/NotificationAddedToChannelEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Channel#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>fun [NotificationAddedToChannelEvent](NotificationAddedToChannelEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channel: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md), totalUnreadCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, unreadChannels: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0)|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/channel/#/PointingToDeclaration/"></a>[channel](channel.md)| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/channel/#/PointingToDeclaration/"></a>open override val [channel](channel.md): [Channel](../../io.getstream.chat.android.client.models/Channel/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/channelId/#/PointingToDeclaration/"></a>@SerializedName(value = channel_id)open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/channelType/#/PointingToDeclaration/"></a>@SerializedName(value = channel_type)open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/cid/#/PointingToDeclaration/"></a>open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/totalUnreadCount/#/PointingToDeclaration/"></a>[totalUnreadCount](totalUnreadCount.md)| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/totalUnreadCount/#/PointingToDeclaration/"></a>@SerializedName(value = total_unread_count)open override val [totalUnreadCount](totalUnreadCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/unreadChannels/#/PointingToDeclaration/"></a>[unreadChannels](unreadChannels.md)| <a name="io.getstream.chat.android.client.events/NotificationAddedToChannelEvent/unreadChannels/#/PointingToDeclaration/"></a>@SerializedName(value = unread_channels)open override val [unreadChannels](unreadChannels.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0|

