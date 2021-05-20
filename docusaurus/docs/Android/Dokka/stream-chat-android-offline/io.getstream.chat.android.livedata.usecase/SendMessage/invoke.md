---
title: invoke
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[SendMessage](index.md)/[invoke](invoke.md)  
  
  
  
# invoke  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()open operator fun [invoke](invoke.md)(message: Message): Call&lt;Message&gt;Sends the message. Immediately adds the message to local storage API call to send the message is retried according to the retry policy specified on the chatDomain  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.utils.RetryPolicy](../../io.getstream.chat.android.livedata.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>|
  
  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>the message to send|
  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()abstract operator fun [invoke](invoke.md)(message: Message, attachmentTransformer: (at: Attachment, file: [File](https://developer.android.com/reference/kotlin/java/io/File.html)) -&gt; Attachment?): Call&lt;Message&gt;Sends the message. Immediately adds the message to local storage API call to send the message is retried according to the retry policy specified on the chatDomain  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message#kotlin.Function2[io.getstream.chat.android.client.models.Attachment,java.io.File,io.getstream.chat.android.client.models.Attachment]?/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.utils.RetryPolicy](../../io.getstream.chat.android.livedata.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message#kotlin.Function2[io.getstream.chat.android.client.models.Attachment,java.io.File,io.getstream.chat.android.client.models.Attachment]?/PointingToDeclaration/"></a>|
  
  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message#kotlin.Function2[io.getstream.chat.android.client.models.Attachment,java.io.File,io.getstream.chat.android.client.models.Attachment]?/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message#kotlin.Function2[io.getstream.chat.android.client.models.Attachment,java.io.File,io.getstream.chat.android.client.models.Attachment]?/PointingToDeclaration/"></a>the message to send|
  

