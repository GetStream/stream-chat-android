---
title: isPermanent
---
//[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.offline.extensions](index.md)/[isPermanent](isPermanent.md)



# isPermanent  
[androidJvm]  
Content  
fun ChatError.[isPermanent](isPermanent.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  
More info  


Returns true if an error is a permanent failure instead of a temporary one (broken network, 500, rate limit etc.)



A permanent error is an error returned by Stream's API (IE a validation error on the input) Any permanent error will always have a stream error code



Temporary errors are retried. Network not being available is a common example of a temporary error.



See the error codes here https://getstream.io/chat/docs/api_errors_response/?language=js

  



