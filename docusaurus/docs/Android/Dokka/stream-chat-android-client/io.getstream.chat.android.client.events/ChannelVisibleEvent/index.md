---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[ChannelVisibleEvent](index.md)  
  
  
  
# ChannelVisibleEvent  
data class [ChannelVisibleEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md)Triggered when a channel is made visible  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/ChannelVisibleEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User/PointingToDeclaration/"></a>[ChannelVisibleEvent](ChannelVisibleEvent.md)| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/ChannelVisibleEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User/PointingToDeclaration/"></a>fun [ChannelVisibleEvent](ChannelVisibleEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md))|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/channelId/#/PointingToDeclaration/"></a>@SerializedName(value = channel_id)open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/channelType/#/PointingToDeclaration/"></a>@SerializedName(value = channel_type)open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/cid/#/PointingToDeclaration/"></a>open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/ChannelVisibleEvent/user/#/PointingToDeclaration/"></a>open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)|

