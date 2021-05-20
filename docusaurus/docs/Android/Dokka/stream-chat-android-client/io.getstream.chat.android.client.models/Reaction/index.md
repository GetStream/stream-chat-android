---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.models](../index.md)/[Reaction](index.md)  
  
  
  
# Reaction  
data class [Reaction](index.md)(**messageId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **score**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **user**: [User](../User/index.md)?, **userId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?, **updatedAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?, **deletedAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?, **syncStatus**: [SyncStatus](../../io.getstream.chat.android.client.utils/SyncStatus/index.md), **extraData**: [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;, **enforceUnique**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) : [CustomObject](../CustomObject/index.md)  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.models/Reaction/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.models/Reaction/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)var [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null|
| <a name="io.getstream.chat.android.client.models/Reaction/deletedAt/#/PointingToDeclaration/"></a>[deletedAt](deletedAt.md)| <a name="io.getstream.chat.android.client.models/Reaction/deletedAt/#/PointingToDeclaration/"></a>var [deletedAt](deletedAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null|
| <a name="io.getstream.chat.android.client.models/Reaction/enforceUnique/#/PointingToDeclaration/"></a>[enforceUnique](enforceUnique.md)| <a name="io.getstream.chat.android.client.models/Reaction/enforceUnique/#/PointingToDeclaration/"></a>var [enforceUnique](enforceUnique.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false|
| <a name="io.getstream.chat.android.client.models/Reaction/extraData/#/PointingToDeclaration/"></a>[extraData](extraData.md)| <a name="io.getstream.chat.android.client.models/Reaction/extraData/#/PointingToDeclaration/"></a>open override var [extraData](extraData.md): [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;|
| <a name="io.getstream.chat.android.client.models/Reaction/messageId/#/PointingToDeclaration/"></a>[messageId](messageId.md)| <a name="io.getstream.chat.android.client.models/Reaction/messageId/#/PointingToDeclaration/"></a>@SerializedName(value = message_id)var [messageId](messageId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.models/Reaction/score/#/PointingToDeclaration/"></a>[score](score.md)| <a name="io.getstream.chat.android.client.models/Reaction/score/#/PointingToDeclaration/"></a>var [score](score.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0|
| <a name="io.getstream.chat.android.client.models/Reaction/syncStatus/#/PointingToDeclaration/"></a>[syncStatus](syncStatus.md)| <a name="io.getstream.chat.android.client.models/Reaction/syncStatus/#/PointingToDeclaration/"></a>var [syncStatus](syncStatus.md): [SyncStatus](../../io.getstream.chat.android.client.utils/SyncStatus/index.md)|
| <a name="io.getstream.chat.android.client.models/Reaction/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.models/Reaction/type/#/PointingToDeclaration/"></a>var [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.models/Reaction/updatedAt/#/PointingToDeclaration/"></a>[updatedAt](updatedAt.md)| <a name="io.getstream.chat.android.client.models/Reaction/updatedAt/#/PointingToDeclaration/"></a>@SerializedName(value = updated_at)var [updatedAt](updatedAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null|
| <a name="io.getstream.chat.android.client.models/Reaction/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.models/Reaction/user/#/PointingToDeclaration/"></a>var [user](user.md): [User](../User/index.md)? = null|
| <a name="io.getstream.chat.android.client.models/Reaction/userId/#/PointingToDeclaration/"></a>[userId](userId.md)| <a name="io.getstream.chat.android.client.models/Reaction/userId/#/PointingToDeclaration/"></a>@SerializedName(value = user_id)var [userId](userId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.models/Reaction/fetchUserId/#/PointingToDeclaration/"></a>[fetchUserId](fetchUserId.md)| <a name="io.getstream.chat.android.client.models/Reaction/fetchUserId/#/PointingToDeclaration/"></a>fun [fetchUserId](fetchUserId.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
  
  
## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.models/CustomObject/getExtraValue/#kotlin.String#TypeParam(bounds=[kotlin.Any?])/PointingToDeclaration/"></a>[getExtraValue](../CustomObject/getExtraValue.md)| <a name="io.getstream.chat.android.client.models/CustomObject/getExtraValue/#kotlin.String#TypeParam(bounds=[kotlin.Any?])/PointingToDeclaration/"></a>open fun &lt;[T](../CustomObject/getExtraValue.md)&gt; [getExtraValue](../CustomObject/getExtraValue.md)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), default: [T](../CustomObject/getExtraValue.md)): [T](../CustomObject/getExtraValue.md)|
| <a name="io.getstream.chat.android.client.models/CustomObject/putExtraValue/#kotlin.String#kotlin.Any/PointingToDeclaration/"></a>[putExtraValue](../CustomObject/putExtraValue.md)| <a name="io.getstream.chat.android.client.models/CustomObject/putExtraValue/#kotlin.String#kotlin.Any/PointingToDeclaration/"></a>open fun [putExtraValue](../CustomObject/putExtraValue.md)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), value: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html))|

