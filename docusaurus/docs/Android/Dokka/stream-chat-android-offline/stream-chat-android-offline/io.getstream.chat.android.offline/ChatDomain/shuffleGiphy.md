---
title: shuffleGiphy
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[shuffleGiphy](shuffleGiphy.md)



# shuffleGiphy  
[androidJvm]  
Content  
abstract fun [shuffleGiphy](shuffleGiphy.md)(message: Message): Call&lt;Message&gt;  
More info  


Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage. Returns new "ephemeral" message with new giphy url. API call to remove the message is retried according to the retry policy specified on the chatDomain



#### Return  


executable async Call responsible for shuffling Giphy image



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/shuffleGiphy/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[io.getstream.chat.android.offline.utils.RetryPolicy](../../io.getstream.chat.android.offline.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.offline/ChatDomain/shuffleGiphy/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/shuffleGiphy/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.offline/ChatDomain/shuffleGiphy/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a><br/><br/>the message to send<br/><br/>|
  
  



