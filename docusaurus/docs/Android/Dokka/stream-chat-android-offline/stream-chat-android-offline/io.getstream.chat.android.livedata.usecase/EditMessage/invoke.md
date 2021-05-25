---
title: invoke
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[EditMessage](index.md)/[invoke](invoke.md)



# invoke  
[androidJvm]  
Content  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  
  
abstract operator fun [invoke](invoke.md)(message: Message): Call&lt;Message&gt;  
More info  


Edits the specified message. Local storage is updated immediately The API request is retried according to the retry policy specified on the chatDomain



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/EditMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.utils.RetryPolicy](../../io.getstream.chat.android.livedata.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.livedata.usecase/EditMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/EditMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.livedata.usecase/EditMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a><br/><br/>the message to edit<br/><br/>|
  
  



