---
title: watchChannel
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChatDomain](index.md)/[watchChannel](watchChannel.md)  
  
  
  
# watchChannel  
abstract fun [watchChannel](watchChannel.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;[ChannelController](../../io.getstream.chat.android.livedata.controller/ChannelController/index.md)&gt;Watches the given channel and returns a ChannelController  
  
#### Return  
executable async Call responsible for obtaining [ChannelController](../../io.getstream.chat.android.livedata.controller/ChannelController/index.md)  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.controller.ChannelController](../../io.getstream.chat.android.livedata.controller/ChannelController/index.md)| <a name="io.getstream.chat.android.livedata/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>|
  
  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>the full channel id. ie messaging:123|
| <a name="io.getstream.chat.android.livedata/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.livedata/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>how many messages to load on the first request|
  

