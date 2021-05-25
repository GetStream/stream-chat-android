---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[NewMessageEvent](index.md)



# NewMessageEvent  
 [androidJvm] data class [NewMessageEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **message**: [Message](../../io.getstream.chat.android.client.models/Message/index.md), **watcherCount**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **totalUnreadCount**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **unreadChannels**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md), [HasMessage](../HasMessage/index.md), [HasWatcherCount](../HasWatcherCount/index.md), [HasUnreadCounts](../HasUnreadCounts/index.md)

Triggered when a new message is added on a channel.

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/NewMessageEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message#kotlin.Int#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[NewMessageEvent](NewMessageEvent.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/NewMessageEvent/#kotlin.String#java.util.Date#io.getstream.chat.android.client.models.User#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.Message#kotlin.Int#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a> [androidJvm] fun [NewMessageEvent](NewMessageEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), message: [Message](../../io.getstream.chat.android.client.models/Message/index.md), watcherCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, totalUnreadCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, unreadChannels: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0)   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/message/#/PointingToDeclaration/"></a>[message](message.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/message/#/PointingToDeclaration/"></a> [androidJvm] open override val [message](message.md): [Message](../../io.getstream.chat.android.client.models/Message/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/totalUnreadCount/#/PointingToDeclaration/"></a>[totalUnreadCount](totalUnreadCount.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/totalUnreadCount/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = total_unread_count)  <br/>  <br/>open override val [totalUnreadCount](totalUnreadCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/unreadChannels/#/PointingToDeclaration/"></a>[unreadChannels](unreadChannels.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/unreadChannels/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = unread_channels)  <br/>  <br/>open override val [unreadChannels](unreadChannels.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/user/#/PointingToDeclaration/"></a> [androidJvm] open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/NewMessageEvent/watcherCount/#/PointingToDeclaration/"></a>[watcherCount](watcherCount.md)| <a name="io.getstream.chat.android.client.events/NewMessageEvent/watcherCount/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = watcher_count)  <br/>  <br/>open override val [watcherCount](watcherCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|

