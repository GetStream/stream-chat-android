---
title: invoke
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[LoadNewerMessages](index.md)/[invoke](invoke.md)



# invoke  
[androidJvm]  
Content  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  
  
abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;Channel&gt;  
More info  


Loads newer messages for the channel



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/LoadNewerMessages/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/LoadNewerMessages/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: the full channel id i. e. messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.livedata.usecase/LoadNewerMessages/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.livedata.usecase/LoadNewerMessages/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: how many new messages to load<br/><br/>|
  
  



