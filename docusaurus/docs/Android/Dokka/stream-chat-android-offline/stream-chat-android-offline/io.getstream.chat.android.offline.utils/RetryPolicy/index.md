---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline.utils](../index.md)/[RetryPolicy](index.md)



# RetryPolicy  
 [androidJvm] interface [RetryPolicy](index.md)

When creating a channel, adding a reaction or sending any temporary error will trigger the retry policy The retry policy interface exposes 2 methods

<ul><li>shouldRetry: returns a boolean if the request should be retried</li><li>retryTimeout: How many milliseconds to wait till the next attempt</li></ul>   


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.utils/RetryPolicy/retryTimeout/#io.getstream.chat.android.client.ChatClient#kotlin.Int#io.getstream.chat.android.client.errors.ChatError/PointingToDeclaration/"></a>[retryTimeout](retryTimeout.md)| <a name="io.getstream.chat.android.offline.utils/RetryPolicy/retryTimeout/#io.getstream.chat.android.client.ChatClient#kotlin.Int#io.getstream.chat.android.client.errors.ChatError/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>abstract fun [retryTimeout](retryTimeout.md)(client: ChatClient, attempt: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), error: ChatError): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br/>More info  <br/>In the case that we want to retry a failed request the retryTimeout method is called to determine the timeout  <br/><br/><br/>|
| <a name="io.getstream.chat.android.offline.utils/RetryPolicy/shouldRetry/#io.getstream.chat.android.client.ChatClient#kotlin.Int#io.getstream.chat.android.client.errors.ChatError/PointingToDeclaration/"></a>[shouldRetry](shouldRetry.md)| <a name="io.getstream.chat.android.offline.utils/RetryPolicy/shouldRetry/#io.getstream.chat.android.client.ChatClient#kotlin.Int#io.getstream.chat.android.client.errors.ChatError/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>abstract fun [shouldRetry](shouldRetry.md)(client: ChatClient, attempt: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), error: ChatError): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br/>More info  <br/>Should Retry evaluates if we should retry the failure  <br/><br/><br/>|


## Inheritors  
  
|  Name | 
|---|
| <a name="io.getstream.chat.android.livedata.utils/RetryPolicy///PointingToDeclaration/"></a>[RetryPolicy](../../io.getstream.chat.android.livedata.utils/RetryPolicy/index.md)|

