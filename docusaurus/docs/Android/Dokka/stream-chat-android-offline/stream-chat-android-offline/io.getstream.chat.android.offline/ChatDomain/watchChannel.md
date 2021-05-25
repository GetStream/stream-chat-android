---
title: watchChannel
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[watchChannel](watchChannel.md)



# watchChannel  
[androidJvm]  
Content  
abstract fun [watchChannel](watchChannel.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;[ChannelController](../../io.getstream.chat.android.offline.channel/ChannelController/index.md)&gt;  
More info  


Watches the given channel and returns a ChannelController



#### Return  


executable async Call responsible for obtaining [ChannelController](../../io.getstream.chat.android.offline.channel/ChannelController/index.md)



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>[io.getstream.chat.android.offline.channel.ChannelController](../../io.getstream.chat.android.offline.channel/ChannelController/index.md)| <a name="io.getstream.chat.android.offline/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.offline/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a><br/><br/>the full channel id. ie messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.offline/ChatDomain/watchChannel/#kotlin.String#kotlin.Int/PointingToDeclaration/"></a><br/><br/>how many messages to load on the first request<br/><br/>|
  
  



