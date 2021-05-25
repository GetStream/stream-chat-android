---
title: createDefaultUserBitmapBlocking
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[createDefaultUserBitmapBlocking](createDefaultUserBitmapBlocking.md)



# createDefaultUserBitmapBlocking  
[androidJvm]  
Content  
open fun [createDefaultUserBitmapBlocking](createDefaultUserBitmapBlocking.md)(user: User, style: [AvatarStyle](../AvatarStyle/index.md), @[Px](https://developer.android.com/reference/kotlin/androidx/annotation/Px.html)()avatarSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [Bitmap](https://developer.android.com/reference/kotlin/android/graphics/Bitmap.html)  
More info  


Load a default Bitmap with the specified [avatarSize](createDefaultUserBitmapBlocking.md) to represent the [user](createDefaultUserBitmapBlocking.md). This should be a process that can never fail (e.g. not depend on network).



This method takes precedence over [createDefaultUserBitmap](createDefaultUserBitmap.md) if both are implemented.



Override this method only if you can't provide a suspending implementation, otherwise override [createDefaultUserBitmap](createDefaultUserBitmap.md) instead.



#### Return  


The loaded bitmap.

  



