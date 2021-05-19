---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.logger](../index.md)/[ChatLogger](index.md)  
  
  
  
# ChatLogger  
interface [ChatLogger](index.md)  
  
## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.logger/ChatLogger.Builder///PointingToDeclaration/"></a>[Builder](Builder/index.md)| <a name="io.getstream.chat.android.client.logger/ChatLogger.Builder///PointingToDeclaration/"></a>class [Builder](Builder/index.md)(**config**: [ChatLogger.Config](Config/index.md))|
| <a name="io.getstream.chat.android.client.logger/ChatLogger.Companion///PointingToDeclaration/"></a>[Companion](Companion/index.md)| <a name="io.getstream.chat.android.client.logger/ChatLogger.Companion///PointingToDeclaration/"></a>object [Companion](Companion/index.md)|
| <a name="io.getstream.chat.android.client.logger/ChatLogger.Config///PointingToDeclaration/"></a>[Config](Config/index.md)| <a name="io.getstream.chat.android.client.logger/ChatLogger.Config///PointingToDeclaration/"></a>data class [Config](Config/index.md)(**level**: [ChatLogLevel](../ChatLogLevel/index.md), **handler**: [ChatLoggerHandler](../ChatLoggerHandler/index.md)?)|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.logger/ChatLogger/getLevel/#/PointingToDeclaration/"></a>[getLevel](getLevel.md)| <a name="io.getstream.chat.android.client.logger/ChatLogger/getLevel/#/PointingToDeclaration/"></a>abstract fun [getLevel](getLevel.md)(): [ChatLogLevel](../ChatLogLevel/index.md)|
| <a name="io.getstream.chat.android.client.logger/ChatLogger/logD/#kotlin.Any#kotlin.String/PointingToDeclaration/"></a>[logD](logD.md)| <a name="io.getstream.chat.android.client.logger/ChatLogger/logD/#kotlin.Any#kotlin.String/PointingToDeclaration/"></a>abstract fun [logD](logD.md)(tag: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))|
| <a name="io.getstream.chat.android.client.logger/ChatLogger/logE/#kotlin.Any#io.getstream.chat.android.client.errors.ChatError/PointingToDeclaration/"></a>[logE](logE.md)| <a name="io.getstream.chat.android.client.logger/ChatLogger/logE/#kotlin.Any#io.getstream.chat.android.client.errors.ChatError/PointingToDeclaration/"></a>abstract fun [logE](logE.md)(tag: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), chatError: ChatError)abstract fun [logE](logE.md)(tag: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))abstract fun [logE](logE.md)(tag: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), throwable: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html))abstract fun [logE](logE.md)(tag: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), chatError: ChatError)abstract fun [logE](logE.md)(tag: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), throwable: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html))|
| <a name="io.getstream.chat.android.client.logger/ChatLogger/logI/#kotlin.Any#kotlin.String/PointingToDeclaration/"></a>[logI](logI.md)| <a name="io.getstream.chat.android.client.logger/ChatLogger/logI/#kotlin.Any#kotlin.String/PointingToDeclaration/"></a>abstract fun [logI](logI.md)(tag: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))|
| <a name="io.getstream.chat.android.client.logger/ChatLogger/logW/#kotlin.Any#kotlin.String/PointingToDeclaration/"></a>[logW](logW.md)| <a name="io.getstream.chat.android.client.logger/ChatLogger/logW/#kotlin.Any#kotlin.String/PointingToDeclaration/"></a>abstract fun [logW](logW.md)(tag: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))|

