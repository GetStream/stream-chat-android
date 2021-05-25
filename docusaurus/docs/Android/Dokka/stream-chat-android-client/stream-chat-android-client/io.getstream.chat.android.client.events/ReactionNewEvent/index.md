---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[ReactionNewEvent](index.md)



# ReactionNewEvent  
 [androidJvm] data class [ReactionNewEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **message**: [Message](../../io.getstream.chat.android.client.models/Message/index.md), **reaction**: [Reaction](../../io.getstream.chat.android.client.models/Reaction/index.md)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md), [HasMessage](../HasMessage/index.md), [HasReaction](../HasReaction/index.md)

Triggered when a message reaction is added

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/ReactionNewEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>[ReactionNewEvent](ReactionNewEvent.md)| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/ReactionNewEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a> [androidJvm] fun [ReactionNewEvent](ReactionNewEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), message: [Message](../../io.getstream.chat.android.client.models/Message/index.md), reaction: [Reaction](../../io.getstream.chat.android.client.models/Reaction/index.md))   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/message/#/PointingToDeclaration/"></a>[message](message.md)| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/message/#/PointingToDeclaration/"></a> [androidJvm] open override val [message](message.md): [Message](../../io.getstream.chat.android.client.models/Message/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/reaction/#/PointingToDeclaration/"></a>[reaction](reaction.md)| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/reaction/#/PointingToDeclaration/"></a> [androidJvm] open override val [reaction](reaction.md): [Reaction](../../io.getstream.chat.android.client.models/Reaction/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/ReactionNewEvent/user/#/PointingToDeclaration/"></a> [androidJvm] open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)   <br/>|

