---
title: truncateChannel
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client](../index.md)/[ChatClient](index.md)/[truncateChannel](truncateChannel.md)



# truncateChannel  
[androidJvm]  
Content  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  
  
fun [truncateChannel](truncateChannel.md)(channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): Call&lt;[Channel](../../io.getstream.chat.android.client.models/Channel/index.md)&gt;  
More info  


Removes all of the messages of the channel but doesn't affect the channel data or members.



#### Return  


executable async Call which completes with Result having data equal to the truncated channel if the channel was successfully truncated.



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/truncateChannel/#kotlin.String#kotlin.String/PointingToDeclaration/"></a>channelType| <a name="io.getstream.chat.android.client/ChatClient/truncateChannel/#kotlin.String#kotlin.String/PointingToDeclaration/"></a><br/><br/>the channel type. ie messaging<br/><br/>|
| <a name="io.getstream.chat.android.client/ChatClient/truncateChannel/#kotlin.String#kotlin.String/PointingToDeclaration/"></a>channelId| <a name="io.getstream.chat.android.client/ChatClient/truncateChannel/#kotlin.String#kotlin.String/PointingToDeclaration/"></a><br/><br/>the channel id. ie 123<br/><br/>|
  
  



