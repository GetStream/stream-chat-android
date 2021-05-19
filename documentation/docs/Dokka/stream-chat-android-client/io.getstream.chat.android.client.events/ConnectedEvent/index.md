---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[ConnectedEvent](index.md)  
  
  
  
# ConnectedEvent  
data class [ConnectedEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **me**: [User](../../io.getstream.chat.android.client.models/User/index.md), **connectionId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [ChatEvent](../ChatEvent/index.md), [HasOwnUser](../HasOwnUser/index.md)Triggered when a user gets connected to the WS  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/ConnectedEvent/ConnectedEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>[ConnectedEvent](ConnectedEvent.md)| <a name="io.getstream.chat.android.client.events/ConnectedEvent/ConnectedEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>fun [ConnectedEvent](ConnectedEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), me: [User](../../io.getstream.chat.android.client.models/User/index.md), connectionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/ConnectedEvent/connectionId/#/PointingToDeclaration/"></a>[connectionId](connectionId.md)| <a name="io.getstream.chat.android.client.events/ConnectedEvent/connectionId/#/PointingToDeclaration/"></a>@SerializedName(value = connection_id)val [connectionId](connectionId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/ConnectedEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/ConnectedEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/ConnectedEvent/me/#/PointingToDeclaration/"></a>[me](me.md)| <a name="io.getstream.chat.android.client.events/ConnectedEvent/me/#/PointingToDeclaration/"></a>open override val [me](me.md): [User](../../io.getstream.chat.android.client.models/User/index.md)|
| <a name="io.getstream.chat.android.client.events/ConnectedEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/ConnectedEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|

