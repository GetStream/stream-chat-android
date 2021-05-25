---
title: sendFile
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client](../index.md)/[ChatClient](index.md)/[sendFile](sendFile.md)



# sendFile  
[androidJvm]  
Content  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  
@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()  
  
fun [sendFile](sendFile.md)(channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), file: [File](https://developer.android.com/reference/kotlin/java/io/File.html), callback: [ProgressCallback](../../io.getstream.chat.android.client.utils/ProgressCallback/index.md)? = null): Call&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;  
More info  


Uploads a file for the given channel. Progress can be accessed via [callback](sendFile.md).



The Stream CDN imposes the following restrictions on file uploads:

<ul><li>The maximum file size is 20 MB</li></ul>

#### Return  


executable async Call which completes with Result having data equal to the URL of the uploaded file if the file was successfully uploaded.



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a>[io.getstream.chat.android.client.uploader.FileUploader](../../io.getstream.chat.android.client.uploader/FileUploader/index.md)| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a>| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a><br/><br/>&lt;a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin"&gt;File Uploads&lt;/a&gt;<br/><br/>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a>channelType| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a><br/><br/>the channel type. ie messaging<br/><br/>|
| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a>channelId| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a><br/><br/>the channel id. ie 123<br/><br/>|
| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a>file| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a><br/><br/>the file that needs to be uploaded<br/><br/>|
| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a>callback| <a name="io.getstream.chat.android.client/ChatClient/sendFile/#kotlin.String#kotlin.String#java.io.File#io.getstream.chat.android.client.utils.ProgressCallback?/PointingToDeclaration/"></a><br/><br/>the callback to track progress<br/><br/>|
  
  



