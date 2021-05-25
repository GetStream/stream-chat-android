---
title: sendFile
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.uploader](../index.md)/[FileUploader](index.md)/[sendFile](sendFile.md)



# sendFile  
[androidJvm]  
Content  
abstract fun [sendFile](sendFile.md)(channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), userId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), connectionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), file: [File](https://developer.android.com/reference/kotlin/java/io/File.html), callback: [ProgressCallback](../../io.getstream.chat.android.client.utils/ProgressCallback/index.md)): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?  
More info  


Uploads a file for the given channel. Progress can be accessed via [callback](sendFile.md).



#### Return  


The URL of the uploaded file, or null if the upload failed.

  


[androidJvm]  
Content  
abstract fun [sendFile](sendFile.md)(channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), userId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), connectionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), file: [File](https://developer.android.com/reference/kotlin/java/io/File.html)): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?  
More info  


Uploads a file for the given channel.



#### Return  


The URL of the uploaded file, or null if the upload failed.

  



