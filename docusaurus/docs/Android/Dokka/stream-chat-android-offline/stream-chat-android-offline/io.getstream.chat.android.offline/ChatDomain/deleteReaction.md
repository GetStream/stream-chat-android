---
title: deleteReaction
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[deleteReaction](deleteReaction.md)



# deleteReaction  
[androidJvm]  
Content  
abstract fun [deleteReaction](deleteReaction.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), reaction: Reaction): Call&lt;Message&gt;  
More info  


Deletes the specified reaction, request is retried according to the retry policy specified on the chatDomain



#### Return  


executable async Call responsible for deleting reaction



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/deleteReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>[io.getstream.chat.android.offline.utils.RetryPolicy](../../io.getstream.chat.android.offline.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.offline/ChatDomain/deleteReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/deleteReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.offline/ChatDomain/deleteReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a><br/><br/>the full channel id, ie messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/deleteReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>reaction| <a name="io.getstream.chat.android.offline/ChatDomain/deleteReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a><br/><br/>the reaction to mark as deleted<br/><br/>|
  
  



