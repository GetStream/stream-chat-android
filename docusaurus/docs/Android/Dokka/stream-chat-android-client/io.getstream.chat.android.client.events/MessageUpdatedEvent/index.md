---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[MessageUpdatedEvent](index.md)  
  
  
  
# MessageUpdatedEvent  
data class [MessageUpdatedEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **message**: [Message](../../io.getstream.chat.android.client.models/Message/index.md)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md), [HasMessage](../HasMessage/index.md)Triggered when a message is updated  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/MessageUpdatedEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>[MessageUpdatedEvent](MessageUpdatedEvent.md)| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/MessageUpdatedEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message/PointingToDeclaration/"></a>fun [MessageUpdatedEvent](MessageUpdatedEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), message: [Message](../../io.getstream.chat.android.client.models/Message/index.md))|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/channelId/#/PointingToDeclaration/"></a>@SerializedName(value = channel_id)open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/channelType/#/PointingToDeclaration/"></a>@SerializedName(value = channel_type)open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/cid/#/PointingToDeclaration/"></a>open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/message/#/PointingToDeclaration/"></a>[message](message.md)| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/message/#/PointingToDeclaration/"></a>open override val [message](message.md): [Message](../../io.getstream.chat.android.client.models/Message/index.md)|
| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/MessageUpdatedEvent/user/#/PointingToDeclaration/"></a>open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)|

