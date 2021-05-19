---
title: createUserBitmap
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[createUserBitmap](createUserBitmap.md)  
  
  
  
# createUserBitmap  
open suspend fun [createUserBitmap](createUserBitmap.md)(user: User, style: [AvatarStyle](../AvatarStyle/index.md), @[Px](https://developer.android.com/reference/kotlin/androidx/annotation/Px.html)()avatarSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [Bitmap](https://developer.android.com/reference/kotlin/android/graphics/Bitmap.html)?Load a Bitmap with the specified [avatarSize](createUserBitmap.md) to represent the [user](createUserBitmap.md), in a suspending operation.The [createUserBitmapBlocking](createUserBitmapBlocking.md) method takes precedence over this one if both are implemented. Prefer implementing this method if possible.  
  
#### Return  
The loaded bitmap or null if the loading failed (e.g. network issues).
