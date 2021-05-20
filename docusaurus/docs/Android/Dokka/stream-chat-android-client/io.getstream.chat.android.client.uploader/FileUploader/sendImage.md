---
title: sendImage
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.uploader](../index.md)/[FileUploader](index.md)/[sendImage](sendImage.md)  
  
  
  
# sendImage  
abstract fun [sendImage](sendImage.md)(channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), userId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), connectionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), file: [File](https://developer.android.com/reference/kotlin/java/io/File.html), callback: [ProgressCallback](../../io.getstream.chat.android.client.utils/ProgressCallback/index.md)): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?Uploads an image for the given channel. Progress can be accessed via [callback](sendImage.md).  
  
#### Return  
The URL of the uploaded image, or null if the upload failed.abstract fun [sendImage](sendImage.md)(channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), userId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), connectionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), file: [File](https://developer.android.com/reference/kotlin/java/io/File.html)): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?Uploads an image for the given channel.  
  
#### Return  
The URL of the uploaded image, or null if the upload failed.
