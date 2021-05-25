---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[MessageDeletedEvent](index.md)



# MessageDeletedEvent  
 [androidJvm] data class [MessageDeletedEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **message**: [Message](../../io.getstream.chat.android.client.models/Message/index.md)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md), [HasMessage](../HasMessage/index.md)

Triggered when a message is deleted

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/MessageDeletedEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[MessageDeletedEvent](MessageDeletedEvent.md)| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/MessageDeletedEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a> [androidJvm] fun [MessageDeletedEvent](MessageDeletedEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), message: [Message](../../io.getstream.chat.android.client.models/Message/index.md))   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/message/#/PointingToDeclaration/"></a>[message](message.md)| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/message/#/PointingToDeclaration/"></a> [androidJvm] open override val [message](message.md): [Message](../../io.getstream.chat.android.client.models/Message/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/MessageDeletedEvent/user/#/PointingToDeclaration/"></a> [androidJvm] open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)   <br/>|

