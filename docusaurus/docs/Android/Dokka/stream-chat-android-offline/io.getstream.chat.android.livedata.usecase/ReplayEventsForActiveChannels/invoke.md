---
title: invoke
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[ReplayEventsForActiveChannels](index.md)/[invoke](invoke.md)  
  
  
  
# invoke  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;ChatEvent&gt;&gt;Adds the specified channel to the active channels Replays events for all active channels This ensures that your local storage is up to date with the server  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/ReplayEventsForActiveChannels/invoke/#kotlin.String/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/ReplayEventsForActiveChannels/invoke/#kotlin.String/PointingToDeclaration/"></a>: the full channel id i. e. messaging:123|
  

