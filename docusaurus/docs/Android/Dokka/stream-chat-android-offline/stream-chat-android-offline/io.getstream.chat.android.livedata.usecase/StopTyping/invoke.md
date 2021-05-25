---
title: invoke
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[StopTyping](index.md)/[invoke](invoke.md)



# invoke  
[androidJvm]  
Content  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  
  
abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), parentId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null): Call&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;  
More info  


StopTyping should be called when the user submits the text and finishes typing



#### Return  


True when a typing event was sent, false if it wasn't sent.



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/StopTyping/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/StopTyping/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a><br/><br/>: the full channel id i. e. messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.livedata.usecase/StopTyping/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>parentId| <a name="io.getstream.chat.android.livedata.usecase/StopTyping/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a><br/><br/>set this field to message.id to indicate that typing event is happening in a thread<br/><br/>|
  
  



