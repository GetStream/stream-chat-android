---
title: createChannelBitmap
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[createChannelBitmap](createChannelBitmap.md)  
  
  
  
# createChannelBitmap  
open suspend fun [createChannelBitmap](createChannelBitmap.md)(channel: Channel, lastActiveUsers: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;User&gt;, style: [AvatarStyle](../AvatarStyle/index.md), @[Px](https://developer.android.com/reference/kotlin/androidx/annotation/Px.html)()avatarSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [Bitmap](https://developer.android.com/reference/kotlin/android/graphics/Bitmap.html)?Load a Bitmap with the specified [avatarSize](createChannelBitmap.md) to represent the [channel](createChannelBitmap.md), in a suspending operation.The [createChannelBitmapBlocking](createChannelBitmapBlocking.md) method takes precedence over this one if both are implemented. Prefer implementing this method if possible.  
  
#### Return  
The loaded bitmap or null if the loading failed (e.g. network issues).
