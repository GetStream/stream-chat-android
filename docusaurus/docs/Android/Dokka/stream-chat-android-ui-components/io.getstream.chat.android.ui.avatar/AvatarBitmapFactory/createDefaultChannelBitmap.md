---
title: createDefaultChannelBitmap
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[createDefaultChannelBitmap](createDefaultChannelBitmap.md)  
  
  
  
# createDefaultChannelBitmap  
open suspend fun [createDefaultChannelBitmap](createDefaultChannelBitmap.md)(channel: Channel, lastActiveUsers: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;User&gt;, style: [AvatarStyle](../AvatarStyle/index.md), @[Px](https://developer.android.com/reference/kotlin/androidx/annotation/Px.html)()avatarSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [Bitmap](https://developer.android.com/reference/kotlin/android/graphics/Bitmap.html)Load a default Bitmap with the specified [avatarSize](createDefaultChannelBitmap.md) to represent the [channel](createDefaultChannelBitmap.md), in a suspending operation. This should be a process that can never fail (e.g. not depend on network).The [createDefaultChannelBitmapBlocking](createDefaultChannelBitmapBlocking.md) method takes precedence over this one if both are implemented. Prefer implementing this method if possible.  
  
#### Return  
The loaded bitmap.
