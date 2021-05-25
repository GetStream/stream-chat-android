---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[HealthEvent](index.md)



# HealthEvent  
 [androidJvm] data class [HealthEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **connectionId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [ChatEvent](../ChatEvent/index.md)

Triggered every 30 second to confirm that the client connection is still alive

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/HealthEvent/HealthEvent/#kotlin.String#java.util.Date#kotlin.String/PointingToDeclaration/"></a>[HealthEvent](HealthEvent.md)| <a name="io.getstream.chat.android.client.events/HealthEvent/HealthEvent/#kotlin.String#java.util.Date#kotlin.String/PointingToDeclaration/"></a> [androidJvm] fun [HealthEvent](HealthEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), connectionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/HealthEvent/connectionId/#/PointingToDeclaration/"></a>[connectionId](connectionId.md)| <a name="io.getstream.chat.android.client.events/HealthEvent/connectionId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = connection_id)  <br/>  <br/>val [connectionId](connectionId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/HealthEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/HealthEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/HealthEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/HealthEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|

