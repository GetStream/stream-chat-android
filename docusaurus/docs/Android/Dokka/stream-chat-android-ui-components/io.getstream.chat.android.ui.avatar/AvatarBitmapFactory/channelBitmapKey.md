---
title: channelBitmapKey
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.avatar](../index.md)/[AvatarBitmapFactory](index.md)/[channelBitmapKey](channelBitmapKey.md)  
  
  
  
# channelBitmapKey  
open fun [channelBitmapKey](channelBitmapKey.md)(channel: Channel): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?Compute the memory cache key for [channel](channelBitmapKey.md).Items with the same cache key will be treated as equivalent by the memory cache.Returning null will prevent the result of [createChannelBitmap](createChannelBitmap.md) from being added to the memory cache.
