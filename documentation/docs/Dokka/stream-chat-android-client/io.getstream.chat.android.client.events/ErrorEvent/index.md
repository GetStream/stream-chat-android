---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[ErrorEvent](index.md)  
  
  
  
# ErrorEvent  
data class [ErrorEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **error**: ChatError) : [ChatEvent](../ChatEvent/index.md)Triggered when WS connection emits error  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/ErrorEvent/ErrorEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.errors.ChatError/PointingToDeclaration/"></a>[ErrorEvent](ErrorEvent.md)| <a name="io.getstream.chat.android.client.events/ErrorEvent/ErrorEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.errors.ChatError/PointingToDeclaration/"></a>fun [ErrorEvent](ErrorEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), error: ChatError)|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/ErrorEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/ErrorEvent/createdAt/#/PointingToDeclaration/"></a>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/ErrorEvent/error/#/PointingToDeclaration/"></a>[error](error.md)| <a name="io.getstream.chat.android.client.events/ErrorEvent/error/#/PointingToDeclaration/"></a>val [error](error.md): ChatError|
| <a name="io.getstream.chat.android.client.events/ErrorEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/ErrorEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|

