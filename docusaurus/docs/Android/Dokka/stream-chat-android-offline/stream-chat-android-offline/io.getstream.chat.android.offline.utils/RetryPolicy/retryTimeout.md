---
title: retryTimeout
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline.utils](../index.md)/[RetryPolicy](index.md)/[retryTimeout](retryTimeout.md)



# retryTimeout  
[androidJvm]  
Content  
abstract fun [retryTimeout](retryTimeout.md)(client: ChatClient, attempt: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), error: ChatError): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  
More info  


In the case that we want to retry a failed request the retryTimeout method is called to determine the timeout

  



