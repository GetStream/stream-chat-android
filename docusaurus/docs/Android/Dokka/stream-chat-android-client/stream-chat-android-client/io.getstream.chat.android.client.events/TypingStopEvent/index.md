---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[TypingStopEvent](index.md)



# TypingStopEvent  
 [androidJvm] data class [TypingStopEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **parentId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md)

Triggered when a user stops typing

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/TypingStopEvent/TypingStopEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>[TypingStopEvent](TypingStopEvent.md)| <a name="io.getstream.chat.android.client.events/TypingStopEvent/TypingStopEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#kotlin.String?/PointingToDeclaration/"></a> [androidJvm] fun [TypingStopEvent](TypingStopEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), parentId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?)   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/TypingStopEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/TypingStopEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/TypingStopEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/TypingStopEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/TypingStopEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/TypingStopEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/TypingStopEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/TypingStopEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/TypingStopEvent/parentId/#/PointingToDeclaration/"></a>[parentId](parentId.md)| <a name="io.getstream.chat.android.client.events/TypingStopEvent/parentId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = parent_id)  <br/>  <br/>val [parentId](parentId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?   <br/>|
| <a name="io.getstream.chat.android.client.events/TypingStopEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/TypingStopEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/TypingStopEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/TypingStopEvent/user/#/PointingToDeclaration/"></a> [androidJvm] open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)   <br/>|

