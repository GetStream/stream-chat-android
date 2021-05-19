---
title: invoke
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[SendReaction](index.md)/[invoke](invoke.md)  
  
  
  
# invoke  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), reaction: Reaction, enforceUnique: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false): Call&lt;Reaction&gt;Sends the reaction. Immediately adds the reaction to local storage and updates the reaction fields on the related message. API call to send the reaction is retried according to the retry policy specified on the chatDomain  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SendReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.utils.RetryPolicy](../../io.getstream.chat.android.livedata.utils/RetryPolicy/index.md)| <a name="io.getstream.chat.android.livedata.usecase/SendReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>|
  
  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SendReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/SendReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>: the full channel id i. e. messaging:123|
| <a name="io.getstream.chat.android.livedata.usecase/SendReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>reaction| <a name="io.getstream.chat.android.livedata.usecase/SendReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>the reaction to add|
| <a name="io.getstream.chat.android.livedata.usecase/SendReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>enforceUnique| <a name="io.getstream.chat.android.livedata.usecase/SendReaction/invoke/#kotlin.String#io.getstream.chat.android.client.models.Reaction#kotlin.Boolean/PointingToDeclaration/"></a>if set to true, new reaction will replace all reactions the user has on this message|
  

