---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[ChannelUpdatedEvent](index.md)



# ChannelUpdatedEvent  
 [androidJvm] data class [ChannelUpdatedEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **message**: [Message](../../io.getstream.chat.android.client.models/Message/index.md)?, **channel**: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md)) : [CidEvent](../CidEvent/index.md), [HasChannel](../HasChannel/index.md)

Triggered when a channel is updated. Could contain system [message](message.md).

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/ChannelUpdatedEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message?#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a>[ChannelUpdatedEvent](ChannelUpdatedEvent.md)| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/ChannelUpdatedEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message?#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a> [androidJvm] fun [ChannelUpdatedEvent](ChannelUpdatedEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), message: [Message](../../io.getstream.chat.android.client.models/Message/index.md)?, channel: [Channel](../../io.getstream.chat.android.client.models/Channel/index.md))   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/channel/#/PointingToDeclaration/"></a>[channel](channel.md)| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/channel/#/PointingToDeclaration/"></a> [androidJvm] open override val [channel](channel.md): [Channel](../../io.getstream.chat.android.client.models/Channel/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/message/#/PointingToDeclaration/"></a>[message](message.md)| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/message/#/PointingToDeclaration/"></a> [androidJvm] val [message](message.md): [Message](../../io.getstream.chat.android.client.models/Message/index.md)?   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/ChannelUpdatedEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|

