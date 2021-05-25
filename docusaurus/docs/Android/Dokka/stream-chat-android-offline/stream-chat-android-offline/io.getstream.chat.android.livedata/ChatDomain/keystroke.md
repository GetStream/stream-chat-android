---
title: keystroke
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChatDomain](index.md)/[keystroke](keystroke.md)



# keystroke  
[androidJvm]  
Content  
abstract fun [keystroke](keystroke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), parentId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?): Call&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;  
More info  


Keystroke should be called whenever a user enters text into the message input It automatically calls stopTyping when the user stops typing after 5 seconds



#### Return  


executable async Call which completes with [Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/index.html) having data true when a typing event was sent, false if it wasn't sent



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/keystroke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata/ChatDomain/keystroke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a><br/><br/>the full channel id i. e. messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/keystroke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>parentId| <a name="io.getstream.chat.android.livedata/ChatDomain/keystroke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a><br/><br/>set this field to message.id to indicate that typing event is happening in a thread<br/><br/>|
  
  



