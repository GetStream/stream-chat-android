---
title: createDefaultUserBitmap
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[createDefaultUserBitmap](createDefaultUserBitmap.md)  
  
  
  
# createDefaultUserBitmap  
open suspend fun [createDefaultUserBitmap](createDefaultUserBitmap.md)(user: User, style: [AvatarStyle](../AvatarStyle/index.md), @[Px](https://developer.android.com/reference/kotlin/androidx/annotation/Px.html)()avatarSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [Bitmap](https://developer.android.com/reference/kotlin/android/graphics/Bitmap.html)Load a default Bitmap with the specified [avatarSize](createDefaultUserBitmap.md) to represent the [user](createDefaultUserBitmap.md), in a suspending operation. This should be a process that can never fail (e.g. not depend on network).The [createDefaultUserBitmapBlocking](createDefaultUserBitmapBlocking.md) method takes precedence over this one if both are implemented. Prefer implementing this method if possible.  
  
#### Return  
The loaded bitmap.
