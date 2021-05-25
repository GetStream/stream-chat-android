---
title: getMessagesWithAttachments
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client](../index.md)/[ChatClient](index.md)/[getMessagesWithAttachments](getMessagesWithAttachments.md)



# getMessagesWithAttachments  
[androidJvm]  
Content  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  
  
fun [getMessagesWithAttachments](getMessagesWithAttachments.md)(channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), offset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Message](../../io.getstream.chat.android.client.models/Message/index.md)&gt;&gt;  
More info  


Returns a Call&lt;List&lt;Message&gt;&gt; With messages which contain at least one desired type attachment but not necessarily all of them will have a specified type



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a>channelType| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a><br/><br/>the channel type. ie messaging<br/><br/>|
| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a>channelId| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a><br/><br/>the channel id. ie 123<br/><br/>|
| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a>offset| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a><br/><br/>The messages offset<br/><br/>|
| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a>limit| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a><br/><br/>max limit messages to be fetched<br/><br/>|
| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a>type| <a name="io.getstream.chat.android.client/ChatClient/getMessagesWithAttachments/#kotlin.String#kotlin.String#kotlin.Int#kotlin.Int#kotlin.String/PointingToDeclaration/"></a><br/><br/>The desired type attachment<br/><br/>|
  
  



