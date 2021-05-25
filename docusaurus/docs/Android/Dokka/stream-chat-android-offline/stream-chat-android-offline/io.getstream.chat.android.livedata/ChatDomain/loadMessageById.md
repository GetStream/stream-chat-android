---
title: loadMessageById
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChatDomain](index.md)/[loadMessageById](loadMessageById.md)



# loadMessageById  
[androidJvm]  
Content  
abstract fun [loadMessageById](loadMessageById.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), olderMessagesOffset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), newerMessagesOffset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;Message&gt;  
More info  


Loads message for a given message id and channel id



#### Return  


executable async Call responsible for loading a message



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/loadMessageById/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata/ChatDomain/loadMessageById/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: the full channel id i. e. messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/loadMessageById/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>messageId| <a name="io.getstream.chat.android.livedata/ChatDomain/loadMessageById/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: the id of the message<br/><br/>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/loadMessageById/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>olderMessagesOffset| <a name="io.getstream.chat.android.livedata/ChatDomain/loadMessageById/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: how many new messages to load before the requested message<br/><br/>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/loadMessageById/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>newerMessagesOffset| <a name="io.getstream.chat.android.livedata/ChatDomain/loadMessageById/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: how many new messages to load after the requested message<br/><br/>|
  
  



