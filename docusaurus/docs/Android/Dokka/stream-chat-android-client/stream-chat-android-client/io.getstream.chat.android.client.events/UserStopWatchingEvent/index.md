---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[UserStopWatchingEvent](index.md)



# UserStopWatchingEvent  
 [androidJvm] data class [UserStopWatchingEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **watcherCount**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md), [HasWatcherCount](../HasWatcherCount/index.md)

Triggered when a user stops watching a channel

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/UserStopWatchingEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.Int#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User/PointingToDeclaration/"></a>[UserStopWatchingEvent](UserStopWatchingEvent.md)| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/UserStopWatchingEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.Int#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User/PointingToDeclaration/"></a> [androidJvm] fun [UserStopWatchingEvent](UserStopWatchingEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), watcherCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md))   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/channelId/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_id)  <br/>  <br/>open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/channelType/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = channel_type)  <br/>  <br/>open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/cid/#/PointingToDeclaration/"></a> [androidJvm] open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/createdAt/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = created_at)  <br/>  <br/>open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/type/#/PointingToDeclaration/"></a> [androidJvm] open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|
| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/user/#/PointingToDeclaration/"></a> [androidJvm] open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)   <br/>|
| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/watcherCount/#/PointingToDeclaration/"></a>[watcherCount](watcherCount.md)| <a name="io.getstream.chat.android.client.events/UserStopWatchingEvent/watcherCount/#/PointingToDeclaration/"></a> [androidJvm] @SerializedName(value = watcher_count)  <br/>  <br/>open override val [watcherCount](watcherCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br/>|

