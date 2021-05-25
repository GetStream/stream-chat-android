---
title: getNonNullString
---
//[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.extensions](index.md)/[getNonNullString](getNonNullString.md)



# getNonNullString  
[androidJvm]  
Content  
inline fun [SharedPreferences](https://developer.android.com/reference/kotlin/android/content/SharedPreferences.html).[getNonNullString](getNonNullString.md)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), defaultValue: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  
More info  


Unlike the getString method it delegates to, this method requires a non-null default value, and therefore guarantees to return a non-null String.

  



