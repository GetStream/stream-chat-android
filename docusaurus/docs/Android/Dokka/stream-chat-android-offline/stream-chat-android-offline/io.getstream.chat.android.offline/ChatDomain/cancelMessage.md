---
title: cancelMessage
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[cancelMessage](cancelMessage.md)



# cancelMessage  
[androidJvm]  
Content  
abstract fun [cancelMessage](cancelMessage.md)(message: Message): Call&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;  
More info  


Cancels the message of "ephemeral" type. Removes the message from local storage. API call to remove the message is retried according to the retry policy specified on the chatDomain



#### Return  


executable async Call responsible for canceling ephemeral message



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/cancelMessage/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[io.getstream.chat.android.offline.utils.RetryPolicy](../../io.getstream.chat.android.offline.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.offline/ChatDomain/cancelMessage/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/cancelMessage/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.offline/ChatDomain/cancelMessage/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a><br/><br/>the message to send<br/><br/>|
  
  



