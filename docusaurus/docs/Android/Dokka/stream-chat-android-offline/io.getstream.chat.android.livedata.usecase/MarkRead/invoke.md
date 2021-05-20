---
title: invoke
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[MarkRead](index.md)/[invoke](invoke.md)  
  
  
  
# invoke  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): Call&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;Marks the messages on the specified channel as read  
  
#### Return  
True if the mark read event was sent. False if there was no need to mark read     (i. e. the messages are already marked as read).  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/MarkRead/invoke/#kotlin.String/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/MarkRead/invoke/#kotlin.String/PointingToDeclaration/"></a>: the full channel id i. e. messaging:123|
  

