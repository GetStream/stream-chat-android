---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[MarkAllReadEvent](index.md)



# MarkAllReadEvent  
 [androidJvm] data class [MarkAllReadEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md), **totalUnreadCount**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **unreadChannels**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [ChatEvent](../ChatEvent/index.md), [UserEvent](../UserEvent/index.md), [HasUnreadCounts](../HasUnreadCounts/index.md)

Triggered when the total count of unread messages (across all channels the user is a member) changes

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/MarkAllReadEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[MarkAllReadEvent](MarkAllReadEvent.md)| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/MarkAllReadEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a> [androidJvm] fun [MarkAllReadEvent](MarkAllReadEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "", createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md), totalUnreadCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, unreadChannels: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0)   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/totalUnreadCount/#/PointingToDeclaration/"></a>[totalUnreadCount](totalUnreadCount.md)| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/totalUnreadCount/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = total_unread_count)  <br/>  <br/>open override val [totalUnreadCount](totalUnreadCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|
| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/unreadChannels/#/PointingToDeclaration/"></a>[unreadChannels](unreadChannels.md)| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/unreadChannels/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = unread_channels)  <br/>  <br/>open override val [unreadChannels](unreadChannels.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|
| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/MarkAllReadEvent/user/#/PointingToDeclaration/"></a> [androidJvm] open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)   <br/>|

