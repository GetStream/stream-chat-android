---
title: invoke
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[DeleteReaction](index.md)/[invoke](invoke.md)  
  
  
  
# invoke  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), reaction: Reaction): Call&lt;Message&gt;Deletes the specified reaction, request is retried according to the retry policy specified on the chatDomain  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/DeleteReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.utils.RetryPolicy](../../io.getstream.chat.android.livedata.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.livedata.usecase/DeleteReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>|
  
  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/DeleteReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/DeleteReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>the full channel id, ie messaging:123|
| <a name="io.getstream.chat.android.livedata.usecase/DeleteReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>reaction| <a name="io.getstream.chat.android.livedata.usecase/DeleteReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction/PointingToDeclaration/"></a>the reaction to mark as deleted|
  

