---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[UnknownEvent](index.md)  
  
  
  
# UnknownEvent  
data class [UnknownEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **rawData**: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;*, *&gt;) : [ChatEvent](../ChatEvent/index.md)Triggered when event type is not supported  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/UnknownEvent/UnknownEvent/#kotlin.String#java.util.Date#kotlin.collections.Map[*,*]/PointingToDeclaration/"></a>[UnknownEvent](UnknownEvent.md)| <a name="io.getstream.chat.android.client.events/UnknownEvent/UnknownEvent/#kotlin.String#java.util.Date#kotlin.collections.Map[*,*]/PointingToDeclaration/"></a>fun [UnknownEvent](UnknownEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), rawData: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;*, *&gt;)|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/UnknownEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/UnknownEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/UnknownEvent/rawData/#/PointingToDeclaration/"></a>[rawData](rawData.md)| <a name="io.getstream.chat.android.client.events/UnknownEvent/rawData/#/PointingToDeclaration/"></a>val [rawData](rawData.md): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;*, *&gt;|
| <a name="io.getstream.chat.android.client.events/UnknownEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/UnknownEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|

