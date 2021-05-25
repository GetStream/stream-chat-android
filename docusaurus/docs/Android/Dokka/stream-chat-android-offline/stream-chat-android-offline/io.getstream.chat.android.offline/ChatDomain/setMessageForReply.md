---
title: setMessageForReply
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[setMessageForReply](setMessageForReply.md)



# setMessageForReply  
[androidJvm]  
Content  
abstract fun [setMessageForReply](setMessageForReply.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), message: Message?): Call&lt;[Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)&gt;  
More info  


Set the reply state for the channel.



#### Return  


executable async Call



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/setMessageForReply/#kotlin.String#io.getstream.chat.android.client.models.Message?/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.offline/ChatDomain/setMessageForReply/#kotlin.String#io.getstream.chat.android.client.models.Message?/PointingToDeclaration/"></a><br/><br/>CID of the channel where reply state is being set.<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/setMessageForReply/#kotlin.String#io.getstream.chat.android.client.models.Message?/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.offline/ChatDomain/setMessageForReply/#kotlin.String#io.getstream.chat.android.client.models.Message?/PointingToDeclaration/"></a><br/><br/>The message we want reply to. The null value means dismiss reply state.<br/><br/>|
  
  



