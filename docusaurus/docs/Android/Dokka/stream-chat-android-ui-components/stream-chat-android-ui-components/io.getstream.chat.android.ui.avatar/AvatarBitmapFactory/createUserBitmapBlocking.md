---
title: createUserBitmapBlocking
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[createUserBitmapBlocking](createUserBitmapBlocking.md)



# createUserBitmapBlocking  
[androidJvm]  
Content  
open fun [createUserBitmapBlocking](createUserBitmapBlocking.md)(user: User, style: [AvatarStyle](../AvatarStyle/index.md), @[Px](https://developer.android.com/reference/kotlin/androidx/annotation/Px.html)()avatarSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [Bitmap](https://developer.android.com/reference/kotlin/android/graphics/Bitmap.html)?  
More info  


Load a Bitmap with the specified [avatarSize](createUserBitmapBlocking.md) to represent the [user](createUserBitmapBlocking.md).



This method takes precedence over [createUserBitmap](createUserBitmap.md) if both are implemented.



Override this method only if you can't provide a suspending implementation, otherwise override [createUserBitmap](createUserBitmap.md) instead.



#### Return  


The loaded bitmap or null if the loading failed (e.g. network issues).

  



