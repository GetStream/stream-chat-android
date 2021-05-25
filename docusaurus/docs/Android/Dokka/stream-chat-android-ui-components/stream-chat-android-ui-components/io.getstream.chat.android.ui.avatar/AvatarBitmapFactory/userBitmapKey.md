---
title: userBitmapKey
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[userBitmapKey](userBitmapKey.md)



# userBitmapKey  
[androidJvm]  
Content  
open fun [userBitmapKey](userBitmapKey.md)(user: User): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?  
More info  


Compute the memory cache key for [user](userBitmapKey.md).



Items with the same cache key will be treated as equivalent by the memory cache.



Returning null will prevent the result of [createUserBitmap](createUserBitmap.md) from being added to the memory cache.

  



