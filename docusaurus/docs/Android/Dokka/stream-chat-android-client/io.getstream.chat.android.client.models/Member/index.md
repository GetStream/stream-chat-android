---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.models](../index.md)/[Member](index.md)  
  
  
  
# Member  
data class [Member](index.md)(**user**: [User](../User/index.md), **role**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?, **updatedAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?, **isInvited**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)?, **inviteAcceptedAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?, **inviteRejectedAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?, **shadowBanned**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) : [UserEntity](../UserEntity/index.md)  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.models/Member/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.models/Member/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)var [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null|
| <a name="io.getstream.chat.android.client.models/Member/inviteAcceptedAt/#/PointingToDeclaration/"></a>[inviteAcceptedAt](inviteAcceptedAt.md)| <a name="io.getstream.chat.android.client.models/Member/inviteAcceptedAt/#/PointingToDeclaration/"></a>@SerializedName(value = invite_accepted_at)var [inviteAcceptedAt](inviteAcceptedAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null|
| <a name="io.getstream.chat.android.client.models/Member/inviteRejectedAt/#/PointingToDeclaration/"></a>[inviteRejectedAt](inviteRejectedAt.md)| <a name="io.getstream.chat.android.client.models/Member/inviteRejectedAt/#/PointingToDeclaration/"></a>@SerializedName(value = invite_rejected_at)var [inviteRejectedAt](inviteRejectedAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null|
| <a name="io.getstream.chat.android.client.models/Member/isInvited/#/PointingToDeclaration/"></a>[isInvited](isInvited.md)| <a name="io.getstream.chat.android.client.models/Member/isInvited/#/PointingToDeclaration/"></a>@SerializedName(value = invited)var [isInvited](isInvited.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Member/role/#/PointingToDeclaration/"></a>[role](role.md)| <a name="io.getstream.chat.android.client.models/Member/role/#/PointingToDeclaration/"></a>var [role](role.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null|
| <a name="io.getstream.chat.android.client.models/Member/shadowBanned/#/PointingToDeclaration/"></a>[shadowBanned](shadowBanned.md)| <a name="io.getstream.chat.android.client.models/Member/shadowBanned/#/PointingToDeclaration/"></a>@SerializedName(value = shadow_banned)var [shadowBanned](shadowBanned.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false|
| <a name="io.getstream.chat.android.client.models/Member/updatedAt/#/PointingToDeclaration/"></a>[updatedAt](updatedAt.md)| <a name="io.getstream.chat.android.client.models/Member/updatedAt/#/PointingToDeclaration/"></a>@SerializedName(value = updated_at)var [updatedAt](updatedAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null|
| <a name="io.getstream.chat.android.client.models/Member/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.models/Member/user/#/PointingToDeclaration/"></a>open override var [user](user.md): [User](../User/index.md)|
  
  
## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.models/UserEntity/getUserId/#/PointingToDeclaration/"></a>[getUserId](../UserEntity/getUserId.md)| <a name="io.getstream.chat.android.client.models/UserEntity/getUserId/#/PointingToDeclaration/"></a>open fun [getUserId](../UserEntity/getUserId.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|

