---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.models](../index.md)/[Attachment](index.md)  
  
  
  
# Attachment  
data class [Attachment](index.md)(**authorName**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **titleLink**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **thumbUrl**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **imageUrl**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **assetUrl**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **ogUrl**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **mimeType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **fileSize**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **title**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **text**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **image**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **url**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **name**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **fallback**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **upload**: [File](https://developer.android.com/reference/kotlin/java/io/File.html)?, **uploadState**: [Attachment.UploadState](UploadState/index.md)?, **extraData**: [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;) : [CustomObject](../CustomObject/index.md)  
  
## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.models/Attachment.UploadState///PointingToDeclaration/"></a>[UploadState](UploadState/index.md)| <a name="io.getstream.chat.android.client.models/Attachment.UploadState///PointingToDeclaration/"></a>sealed class [UploadState](UploadState/index.md)|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.models/Attachment/assetUrl/#/PointingToDeclaration/"></a>[assetUrl](assetUrl.md)| <a name="io.getstream.chat.android.client.models/Attachment/assetUrl/#/PointingToDeclaration/"></a>@SerializedName(value = asset_url)var [assetUrl](assetUrl.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/authorName/#/PointingToDeclaration/"></a>[authorName](authorName.md)| <a name="io.getstream.chat.android.client.models/Attachment/authorName/#/PointingToDeclaration/"></a>@SerializedName(value = author_name)var [authorName](authorName.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/extraData/#/PointingToDeclaration/"></a>[extraData](extraData.md)| <a name="io.getstream.chat.android.client.models/Attachment/extraData/#/PointingToDeclaration/"></a>open override var [extraData](extraData.md): [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;|
| <a name="io.getstream.chat.android.client.models/Attachment/fallback/#/PointingToDeclaration/"></a>[fallback](fallback.md)| <a name="io.getstream.chat.android.client.models/Attachment/fallback/#/PointingToDeclaration/"></a>@SerializedName(value = fallback)var [fallback](fallback.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/fileSize/#/PointingToDeclaration/"></a>[fileSize](fileSize.md)| <a name="io.getstream.chat.android.client.models/Attachment/fileSize/#/PointingToDeclaration/"></a>@SerializedName(value = file_size)var [fileSize](fileSize.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0|
| <a name="io.getstream.chat.android.client.models/Attachment/image/#/PointingToDeclaration/"></a>[image](image.md)| <a name="io.getstream.chat.android.client.models/Attachment/image/#/PointingToDeclaration/"></a>@SerializedName(value = image)var [image](image.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/imageUrl/#/PointingToDeclaration/"></a>[imageUrl](imageUrl.md)| <a name="io.getstream.chat.android.client.models/Attachment/imageUrl/#/PointingToDeclaration/"></a>@SerializedName(value = image_url)var [imageUrl](imageUrl.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/mimeType/#/PointingToDeclaration/"></a>[mimeType](mimeType.md)| <a name="io.getstream.chat.android.client.models/Attachment/mimeType/#/PointingToDeclaration/"></a>@SerializedName(value = mime_type)var [mimeType](mimeType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/name/#/PointingToDeclaration/"></a>[name](name.md)| <a name="io.getstream.chat.android.client.models/Attachment/name/#/PointingToDeclaration/"></a>@SerializedName(value = name)var [name](name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/ogUrl/#/PointingToDeclaration/"></a>[ogUrl](ogUrl.md)| <a name="io.getstream.chat.android.client.models/Attachment/ogUrl/#/PointingToDeclaration/"></a>@SerializedName(value = og_scrape_url)var [ogUrl](ogUrl.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/text/#/PointingToDeclaration/"></a>[text](text.md)| <a name="io.getstream.chat.android.client.models/Attachment/text/#/PointingToDeclaration/"></a>@SerializedName(value = text)var [text](text.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/thumbUrl/#/PointingToDeclaration/"></a>[thumbUrl](thumbUrl.md)| <a name="io.getstream.chat.android.client.models/Attachment/thumbUrl/#/PointingToDeclaration/"></a>@SerializedName(value = thumb_url)var [thumbUrl](thumbUrl.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/title/#/PointingToDeclaration/"></a>[title](title.md)| <a name="io.getstream.chat.android.client.models/Attachment/title/#/PointingToDeclaration/"></a>@SerializedName(value = title)var [title](title.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/titleLink/#/PointingToDeclaration/"></a>[titleLink](titleLink.md)| <a name="io.getstream.chat.android.client.models/Attachment/titleLink/#/PointingToDeclaration/"></a>@SerializedName(value = title_link)var [titleLink](titleLink.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.models/Attachment/type/#/PointingToDeclaration/"></a>@SerializedName(value = type)var [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/upload/#/PointingToDeclaration/"></a>[upload](upload.md)| <a name="io.getstream.chat.android.client.models/Attachment/upload/#/PointingToDeclaration/"></a>var [upload](upload.md): [File](https://developer.android.com/reference/kotlin/java/io/File.html)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/uploadState/#/PointingToDeclaration/"></a>[uploadState](uploadState.md)| <a name="io.getstream.chat.android.client.models/Attachment/uploadState/#/PointingToDeclaration/"></a>var [uploadState](uploadState.md): [Attachment.UploadState](UploadState/index.md)? = null|
| <a name="io.getstream.chat.android.client.models/Attachment/url/#/PointingToDeclaration/"></a>[url](url.md)| <a name="io.getstream.chat.android.client.models/Attachment/url/#/PointingToDeclaration/"></a>@SerializedName(value = url)var [url](url.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
  
  
## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.models/CustomObject/getExtraValue/#kotlin.String#TypeParam(bounds=[kotlin.Any?])/PointingToDeclaration/"></a>[getExtraValue](../CustomObject/getExtraValue.md)| <a name="io.getstream.chat.android.client.models/CustomObject/getExtraValue/#kotlin.String#TypeParam(bounds=[kotlin.Any?])/PointingToDeclaration/"></a>open fun &lt;[T](../CustomObject/getExtraValue.md)&gt; [getExtraValue](../CustomObject/getExtraValue.md)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), default: [T](../CustomObject/getExtraValue.md)): [T](../CustomObject/getExtraValue.md)|
| <a name="io.getstream.chat.android.client.models/CustomObject/putExtraValue/#kotlin.String#kotlin.Any/PointingToDeclaration/"></a>[putExtraValue](../CustomObject/putExtraValue.md)| <a name="io.getstream.chat.android.client.models/CustomObject/putExtraValue/#kotlin.String#kotlin.Any/PointingToDeclaration/"></a>open fun [putExtraValue](../CustomObject/putExtraValue.md)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), value: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html))|
  
  
## Extensions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.extensions//uploadId/io.getstream.chat.android.client.models.Attachment#/PointingToDeclaration/"></a>[uploadId](../../io.getstream.chat.android.client.extensions/uploadId.md)| <a name="io.getstream.chat.android.client.extensions//uploadId/io.getstream.chat.android.client.models.Attachment#/PointingToDeclaration/"></a>var [Attachment](index.md).[uploadId](../../io.getstream.chat.android.client.extensions/uploadId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?|

