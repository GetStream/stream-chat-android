---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[UserPresenceChangedEvent](index.md)  
  
  
  
# UserPresenceChangedEvent  
data class [UserPresenceChangedEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md)) : [ChatEvent](../ChatEvent/index.md), [UserEvent](../UserEvent/index.md)Triggered when a user status changes (eg. online, offline, away, etc.)  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/UserPresenceChangedEvent/UserPresenceChangedEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User/PointingToDeclaration/"></a>[UserPresenceChangedEvent](UserPresenceChangedEvent.md)| <a name="io.getstream.chat.android.client.events/UserPresenceChangedEvent/UserPresenceChangedEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User/PointingToDeclaration/"></a>fun [UserPresenceChangedEvent](UserPresenceChangedEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md))|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/UserPresenceChangedEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/UserPresenceChangedEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/UserPresenceChangedEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/UserPresenceChangedEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/UserPresenceChangedEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/UserPresenceChangedEvent/user/#/PointingToDeclaration/"></a>open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)|

