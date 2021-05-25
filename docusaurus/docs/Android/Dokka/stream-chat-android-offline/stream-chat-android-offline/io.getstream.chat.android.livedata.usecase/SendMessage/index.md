---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[SendMessage](index.md)



# SendMessage  
 [androidJvm] interface [SendMessage](index.md)   


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[invoke](invoke.md)| <a name="io.getstream.chat.android.livedata.usecase/SendMessage/invoke/#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  <br/>  <br/>open operator fun [invoke](invoke.md)(message: Message): Call&lt;Message&gt;  <br/>@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  <br/>  <br/>abstract operator fun [invoke](invoke.md)(message: Message, attachmentTransformer: (at: Attachment, file: [File](https://developer.android.com/reference/kotlin/java/io/File.html)) -&gt; Attachment?): Call&lt;Message&gt;  <br/>More info  <br/>Sends the message.  <br/><br/><br/>|

