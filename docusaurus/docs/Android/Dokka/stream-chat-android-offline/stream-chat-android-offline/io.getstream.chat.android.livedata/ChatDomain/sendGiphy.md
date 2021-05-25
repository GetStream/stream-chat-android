---
title: sendGiphy
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChatDomain](index.md)/[sendGiphy](sendGiphy.md)



# sendGiphy  
[androidJvm]  
Content  
abstract fun [sendGiphy](sendGiphy.md)(message: Message): Call&lt;Message&gt;  
More info  


Sends selected giphy message to the channel. Removes the original "ephemeral" message from local storage. Returns new "ephemeral" message with new giphy url. API call to remove the message is retried according to the retry policy specified on the chatDomain



#### Return  


executable async Call responsible for sending Giphy



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/sendGiphy/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.utils.RetryPolicy](../../io.getstream.chat.android.livedata.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.livedata/ChatDomain/sendGiphy/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/sendGiphy/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.livedata/ChatDomain/sendGiphy/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a><br/><br/>the message to send<br/><br/>|
  
  



