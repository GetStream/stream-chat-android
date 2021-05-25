---
title: sendMessage
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[sendMessage](sendMessage.md)



# sendMessage  
[androidJvm]  
Content  
abstract fun [sendMessage](sendMessage.md)(message: Message): Call&lt;Message&gt;  
More info  


Sends the message. Immediately adds the message to local storage API call to send the message is retried according to the retry policy specified on the chatDomain



#### Return  


executable async Call responsible for sending a message



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/sendMessage/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[io.getstream.chat.android.offline.utils.RetryPolicy](../../io.getstream.chat.android.offline.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.offline/ChatDomain/sendMessage/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/sendMessage/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.offline/ChatDomain/sendMessage/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a><br/><br/>the message to send<br/><br/>|
  
  


[androidJvm]  
Content  
abstract fun [sendMessage](sendMessage.md)(message: Message, attachmentTransformer: (at: Attachment, file: [File](https://developer.android.com/reference/kotlin/java/io/File.html)) -&gt; Attachment?): Call&lt;Message&gt;  
More info  


Sends the message. Immediately adds the message to local storage API call to send the message is retried according to the retry policy specified on the chatDomain



#### Return  


executable async Call responsible for sending a message



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/sendMessage/#io.getstream.chat.android.client.models.Message#kotlin.Function2[io.getstream.chat.android.client.models.Attachment,java.io.File,io.getstream.chat.android.client.models.Attachment]?/PointingToDeclaration/"></a>[io.getstream.chat.android.offline.utils.RetryPolicy](../../io.getstream.chat.android.offline.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.offline/ChatDomain/sendMessage/#io.getstream.chat.android.client.models.Message#kotlin.Function2[io.getstream.chat.android.client.models.Attachment,java.io.File,io.getstream.chat.android.client.models.Attachment]?/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/sendMessage/#io.getstream.chat.android.client.models.Message#kotlin.Function2[io.getstream.chat.android.client.models.Attachment,java.io.File,io.getstream.chat.android.client.models.Attachment]?/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.offline/ChatDomain/sendMessage/#io.getstream.chat.android.client.models.Message#kotlin.Function2[io.getstream.chat.android.client.models.Attachment,java.io.File,io.getstream.chat.android.client.models.Attachment]?/PointingToDeclaration/"></a><br/><br/>the message to send<br/><br/>|
  
  



