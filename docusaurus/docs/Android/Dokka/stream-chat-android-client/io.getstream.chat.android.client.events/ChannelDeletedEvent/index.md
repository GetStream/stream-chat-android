---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[ChannelDeletedEvent](index.md)  
  
  
  
# ChannelDeletedEvent  
data class [ChannelDeletedEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channel**: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md)?) : [CidEvent](../CidEvent/index.md), [HasChannel](../HasChannel/index.md)Triggered when a channel is deleted  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/ChannelDeletedEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Channel#io.getstream.chat.android.client.models.User?/PointingToDeclaration/"></a>[ChannelDeletedEvent](ChannelDeletedEvent.md)| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/ChannelDeletedEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Channel#io.getstream.chat.android.client.models.User?/PointingToDeclaration/"></a>fun [ChannelDeletedEvent](ChannelDeletedEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channel: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md), user: [User](../../io.getstream.chat.android.client.models/User/index.md)?)|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/channel/#/PointingToDeclaration/"></a>[channel](channel.md)| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/channel/#/PointingToDeclaration/"></a>open override val [channel](channel.md): [Channel](../../io.getstream.chat.android.client.models/Channel/index.md)|
| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/channelId/#/PointingToDeclaration/"></a>@SerializedName(value = channel_id)open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/channelType/#/PointingToDeclaration/"></a>@SerializedName(value = channel_type)open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/cid/#/PointingToDeclaration/"></a>open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/ChannelDeletedEvent/user/#/PointingToDeclaration/"></a>val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)?|

