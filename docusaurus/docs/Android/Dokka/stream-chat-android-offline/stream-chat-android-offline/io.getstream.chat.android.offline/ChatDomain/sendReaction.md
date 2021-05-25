---
title: sendReaction
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[sendReaction](sendReaction.md)



# sendReaction  
[androidJvm]  
Content  
abstract fun [sendReaction](sendReaction.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), reaction: Reaction, enforceUnique: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): Call&lt;Reaction&gt;  
More info  


Sends the reaction. Immediately adds the reaction to local storage and updates the reaction fields on the related message. API call to send the reaction is retried according to the retry policy specified on the chatDomain



#### Return  


executable async Call responsible for sending a reaction



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/sendReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>[io.getstream.chat.android.offline.utils.RetryPolicy](../../io.getstream.chat.android.offline.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.offline/ChatDomain/sendReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/sendReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.offline/ChatDomain/sendReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a><br/><br/>: the full channel id i. e. messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/sendReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>reaction| <a name="io.getstream.chat.android.offline/ChatDomain/sendReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a><br/><br/>the reaction to add<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/sendReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>enforceUnique| <a name="io.getstream.chat.android.offline/ChatDomain/sendReaction/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a><br/><br/>if set to true, new reaction will replace all reactions the user has on this message<br/><br/>|
  
  



