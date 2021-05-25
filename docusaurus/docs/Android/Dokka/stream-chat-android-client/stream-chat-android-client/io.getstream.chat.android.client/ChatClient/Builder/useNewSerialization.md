---
title: useNewSerialization
---
//[stream-chat-android-client](../../../../index.md)/[io.getstream.chat.android.client](../../index.md)/[ChatClient](../index.md)/[Builder](index.md)/[useNewSerialization](useNewSerialization.md)



# useNewSerialization  
[androidJvm]  
Content  
fun [useNewSerialization](useNewSerialization.md)(enabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): [ChatClient.Builder](index.md)  
More info  


A new serialization implementation is now used by default by the SDK.



If you experience any issues with the new implementation, call this builder method with false as the parameter to revert to the old implementation. Note that the old implementation will be removed soon.



To check for issues caused by new serialization, enable error logs using the [logLevel](logLevel.md) method and look for the NEW_SERIALIZATION_ERROR tag in your logs.

  



