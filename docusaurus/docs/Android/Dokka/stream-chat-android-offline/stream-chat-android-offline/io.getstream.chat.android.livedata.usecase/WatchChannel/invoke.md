---
title: invoke
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[WatchChannel](index.md)/[invoke](invoke.md)



# invoke  
[androidJvm]  
Content  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  
  
abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;[ChannelController](../../io.getstream.chat.android.livedata.controller/ChannelController/index.md)&gt;  
More info  


Watches the given channel and returns a ChannelController



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/WatchChannel/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.controller.ChannelController](../../io.getstream.chat.android.livedata.controller/ChannelController/index.md)| <a name="io.getstream.chat.android.livedata.usecase/WatchChannel/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/WatchChannel/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/WatchChannel/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a><br/><br/>the full channel id. ie messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.livedata.usecase/WatchChannel/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.livedata.usecase/WatchChannel/invoke/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a><br/><br/>how many messages to load on the first request<br/><br/>|
  
  



