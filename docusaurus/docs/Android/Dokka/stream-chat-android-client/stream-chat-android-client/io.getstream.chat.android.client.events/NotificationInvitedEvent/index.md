---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[NotificationInvitedEvent](index.md)



# NotificationInvitedEvent  
 [androidJvm] data class [NotificationInvitedEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md), **member**: [Member](../../io.getstream.chat.android.client.models/Member/index.md)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md), [HasMember](../HasMember/index.md)

Triggered when the user is invited to join a channel

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/NotificationInvitedEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.models.Member/PointingToDeclaration/"></a>[NotificationInvitedEvent](NotificationInvitedEvent.md)| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/NotificationInvitedEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.models.Member/PointingToDeclaration/"></a> [androidJvm] fun [NotificationInvitedEvent](NotificationInvitedEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md), member: [Member](../../io.getstream.chat.android.client.models/Member/index.md))   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/member/#/PointingToDeclaration/"></a>[member](member.md)| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/member/#/PointingToDeclaration/"></a> [androidJvm] open override val [member](member.md): [Member](../../io.getstream.chat.android.client.models/Member/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/NotificationInvitedEvent/user/#/PointingToDeclaration/"></a> [androidJvm] open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)   <br/>|

