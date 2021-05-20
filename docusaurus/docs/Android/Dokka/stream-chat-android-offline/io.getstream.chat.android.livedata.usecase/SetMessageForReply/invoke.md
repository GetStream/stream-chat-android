---
title: invoke
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[SetMessageForReply](index.md)/[invoke](invoke.md)  
  
  
  
# invoke  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), message: Message?): Call&lt;[Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)&gt;Set the reply state for the channel.  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SetMessageForReply/invoke/#kotlin.String#io.getstream.chat.android.client.models.Message?/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/SetMessageForReply/invoke/#kotlin.String#io.getstream.chat.android.client.models.Message?/PointingToDeclaration/"></a>CID of the channel where reply state is being set.|
| <a name="io.getstream.chat.android.livedata.usecase/SetMessageForReply/invoke/#kotlin.String#io.getstream.chat.android.client.models.Message?/PointingToDeclaration/"></a>message| <a name="io.getstream.chat.android.livedata.usecase/SetMessageForReply/invoke/#kotlin.String#io.getstream.chat.android.client.models.Message?/PointingToDeclaration/"></a>The message we want reply to. The null value means dismiss reply state.|
  

