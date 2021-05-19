---
title: createDefaultChannelBitmapBlocking
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[createDefaultChannelBitmapBlocking](createDefaultChannelBitmapBlocking.md)  
  
  
  
# createDefaultChannelBitmapBlocking  
open fun [createDefaultChannelBitmapBlocking](createDefaultChannelBitmapBlocking.md)(channel: Channel, lastActiveUsers: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;User&gt;, style: [AvatarStyle](../AvatarStyle/index.md), @[Px](https://developer.android.com/reference/kotlin/androidx/annotation/Px.html)()avatarSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [Bitmap](https://developer.android.com/reference/kotlin/android/graphics/Bitmap.html)Load a default Bitmap with the specified [avatarSize](createDefaultChannelBitmapBlocking.md) to represent the [channel](createDefaultChannelBitmapBlocking.md). This should be a process that can never fail (e.g. not depend on network).This method takes precedence over [createDefaultChannelBitmap](createDefaultChannelBitmap.md) if both are implemented.Override this method only if you can't provide a suspending implementation, otherwise override [createDefaultChannelBitmap](createDefaultChannelBitmap.md) instead.  
  
#### Return  
The loaded bitmap.
