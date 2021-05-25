---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[ChannelHiddenEvent](index.md)



# ChannelHiddenEvent  
 [androidJvm] data class [ChannelHiddenEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md), **clearHistory**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md)

Triggered when a channel is mark as hidden

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/ChannelHiddenEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User#kotlin.Boolean/PointingToDeclaration/"></a>[ChannelHiddenEvent](ChannelHiddenEvent.md)| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/ChannelHiddenEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User#kotlin.Boolean/PointingToDeclaration/"></a> [androidJvm] fun [ChannelHiddenEvent](ChannelHiddenEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md), clearHistory: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/clearHistory/#/PointingToDeclaration/"></a>[clearHistory](clearHistory.md)| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/clearHistory/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = clear_history)  <br/>  <br/>val [clearHistory](clearHistory.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/ChannelHiddenEvent/user/#/PointingToDeclaration/"></a> [androidJvm] open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)   <br/>|

