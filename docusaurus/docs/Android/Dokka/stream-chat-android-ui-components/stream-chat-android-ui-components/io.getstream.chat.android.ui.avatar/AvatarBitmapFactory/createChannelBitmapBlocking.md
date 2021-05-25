---
title: createChannelBitmapBlocking
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[createChannelBitmapBlocking](createChannelBitmapBlocking.md)



# createChannelBitmapBlocking  
[androidJvm]  
Content  
open fun [createChannelBitmapBlocking](createChannelBitmapBlocking.md)(channel: Channel, lastActiveUsers: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;User&gt;, style: [AvatarStyle](../AvatarStyle/index.md), @[Px](https://developer.android.com/reference/kotlin/androidx/annotation/Px.html)()avatarSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [Bitmap](https://developer.android.com/reference/kotlin/android/graphics/Bitmap.html)?  
More info  


Load a Bitmap with the specified [avatarSize](createChannelBitmapBlocking.md) to represent the [channel](createChannelBitmapBlocking.md).



This method takes precedence over [createChannelBitmap](createChannelBitmap.md) if both are implemented.



Override this method only if you can't provide a suspending implementation, otherwise override [createChannelBitmap](createChannelBitmap.md) instead.



#### Return  


The loaded bitmap or null if the loading failed (e.g. network issues).

  



