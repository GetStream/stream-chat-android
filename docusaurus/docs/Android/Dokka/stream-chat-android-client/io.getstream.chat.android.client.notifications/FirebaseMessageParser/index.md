---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.notifications](../index.md)/[FirebaseMessageParser](index.md)  
  
  
  
# FirebaseMessageParser  
interface [FirebaseMessageParser](index.md)  
  
## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.notifications/FirebaseMessageParser.Data///PointingToDeclaration/"></a>[Data](Data/index.md)| <a name="io.getstream.chat.android.client.notifications/FirebaseMessageParser.Data///PointingToDeclaration/"></a>data class [Data](Data/index.md)(**messageId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.notifications/FirebaseMessageParser/isValidRemoteMessage/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>[isValidRemoteMessage](isValidRemoteMessage.md)| <a name="io.getstream.chat.android.client.notifications/FirebaseMessageParser/isValidRemoteMessage/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>abstract fun [isValidRemoteMessage](isValidRemoteMessage.md)(message: RemoteMessage): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)|
| <a name="io.getstream.chat.android.client.notifications/FirebaseMessageParser/parse/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>[parse](parse.md)| <a name="io.getstream.chat.android.client.notifications/FirebaseMessageParser/parse/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>abstract fun [parse](parse.md)(message: RemoteMessage): [FirebaseMessageParser.Data](Data/index.md)|
  
  
## Inheritors  
  
|  Name | 
|---|
| <a name="io.getstream.chat.android.client.notifications/FirebaseMessageParserImpl///PointingToDeclaration/"></a>[FirebaseMessageParserImpl](../FirebaseMessageParserImpl/index.md)|

