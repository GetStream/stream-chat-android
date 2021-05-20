---
title: invoke
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[Keystroke](index.md)/[invoke](invoke.md)  
  
  
  
# invoke  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), parentId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null): Call&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;Keystroke should be called whenever a user enters text into the message input It automatically calls stopTyping when the user stops typing after 5 seconds  
  
#### Return  
True when a typing event was sent, false if it wasn't sent  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/Keystroke/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/Keystroke/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>the full channel id i. e. messaging:123|
| <a name="io.getstream.chat.android.livedata.usecase/Keystroke/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>parentId| <a name="io.getstream.chat.android.livedata.usecase/Keystroke/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>set this field to message.id to indicate that typing event is happening in a thread|
  

