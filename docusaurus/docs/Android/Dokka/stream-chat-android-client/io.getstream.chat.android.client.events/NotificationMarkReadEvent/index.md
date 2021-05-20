---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[NotificationMarkReadEvent](index.md)  
  
  
  
# NotificationMarkReadEvent  
data class [NotificationMarkReadEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **totalUnreadCount**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **unreadChannels**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md), [HasUnreadCounts](../HasUnreadCounts/index.md)Triggered when the count of unread messages for a particular channel changes  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/NotificationMarkReadEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[NotificationMarkReadEvent](NotificationMarkReadEvent.md)| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/NotificationMarkReadEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>fun [NotificationMarkReadEvent](NotificationMarkReadEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), totalUnreadCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, unreadChannels: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0)|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/channelId/#/PointingToDeclaration/"></a>@SerializedName(value = channel_id)open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/channelType/#/PointingToDeclaration/"></a>@SerializedName(value = channel_type)open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/cid/#/PointingToDeclaration/"></a>open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/totalUnreadCount/#/PointingToDeclaration/"></a>[totalUnreadCount](totalUnreadCount.md)| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/totalUnreadCount/#/PointingToDeclaration/"></a>@SerializedName(value = total_unread_count)open override val [totalUnreadCount](totalUnreadCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/unreadChannels/#/PointingToDeclaration/"></a>[unreadChannels](unreadChannels.md)| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/unreadChannels/#/PointingToDeclaration/"></a>@SerializedName(value = unread_channels)open override val [unreadChannels](unreadChannels.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0|
| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/NotificationMarkReadEvent/user/#/PointingToDeclaration/"></a>open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)|

