---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[NotificationRemovedFromChannelEvent](index.md)  
  
  
  
# NotificationRemovedFromChannelEvent  
data class [NotificationRemovedFromChannelEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md)?, **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **member**: [Member](../../io.getstream.chat.android.client.models/Member/index.md)) : [CidEvent](../CidEvent/index.md), [HasMember](../HasMember/index.md)Triggered when a user is removed from the list of channel members  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/NotificationRemovedFromChannelEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User?#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Member/PointingToDeclaration/"></a>[NotificationRemovedFromChannelEvent](NotificationRemovedFromChannelEvent.md)| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/NotificationRemovedFromChannelEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User?#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Member/PointingToDeclaration/"></a>fun [NotificationRemovedFromChannelEvent](NotificationRemovedFromChannelEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md)?, cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), member: [Member](../../io.getstream.chat.android.client.models/Member/index.md))|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/channelId/#/PointingToDeclaration/"></a>@SerializedName(value = channel_id)open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/channelType/#/PointingToDeclaration/"></a>@SerializedName(value = channel_type)open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/cid/#/PointingToDeclaration/"></a>open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/member/#/PointingToDeclaration/"></a>[member](member.md)| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/member/#/PointingToDeclaration/"></a>open override val [member](member.md): [Member](../../io.getstream.chat.android.client.models/Member/index.md)|
| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/NotificationRemovedFromChannelEvent/user/#/PointingToDeclaration/"></a>val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)?|

