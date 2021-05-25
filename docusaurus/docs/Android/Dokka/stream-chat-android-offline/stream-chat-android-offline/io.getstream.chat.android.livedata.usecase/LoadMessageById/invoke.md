---
title: invoke
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[LoadMessageById](index.md)/[invoke](invoke.md)



# invoke  
[androidJvm]  
Content  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  
  
abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), olderMessagesOffset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, newerMessagesOffset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0): Call&lt;Message&gt;  
More info  


Loads message for a given message id and channel id



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/LoadMessageById/invoke/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/LoadMessageById/invoke/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: the full channel id i. e. messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.livedata.usecase/LoadMessageById/invoke/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>messageId| <a name="io.getstream.chat.android.livedata.usecase/LoadMessageById/invoke/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: the id of the message<br/><br/>|
| <a name="io.getstream.chat.android.livedata.usecase/LoadMessageById/invoke/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>olderMessagesOffset| <a name="io.getstream.chat.android.livedata.usecase/LoadMessageById/invoke/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: how many new messages to load before the requested message<br/><br/>|
| <a name="io.getstream.chat.android.livedata.usecase/LoadMessageById/invoke/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>newerMessagesOffset| <a name="io.getstream.chat.android.livedata.usecase/LoadMessageById/invoke/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: how many new messages to load after the requested message<br/><br/>|
  
  



