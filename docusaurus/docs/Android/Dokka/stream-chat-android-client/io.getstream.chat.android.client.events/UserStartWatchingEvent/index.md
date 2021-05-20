---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.events](../index.md)/[UserStartWatchingEvent](index.md)  
  
  
  
# UserStartWatchingEvent  
data class [UserStartWatchingEvent](index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **createdAt**: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), **cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **watcherCount**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **channelType**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **channelId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **user**: [User](../../io.getstream.chat.android.client.models/User/index.md)) : [CidEvent](../CidEvent/index.md), [UserEvent](../UserEvent/index.md), [HasWatcherCount](../HasWatcherCount/index.md)Triggered when a user starts watching a channel  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/UserStartWatchingEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.Int#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User/PointingToDeclaration/"></a>[UserStartWatchingEvent](UserStartWatchingEvent.md)| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/UserStartWatchingEvent/#kotlin.String#java.util.Date#kotlin.String#kotlin.Int#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User/PointingToDeclaration/"></a>fun [UserStartWatchingEvent](UserStartWatchingEvent.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), watcherCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, channelType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), user: [User](../../io.getstream.chat.android.client.models/User/index.md))|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/channelId/#/PointingToDeclaration/"></a>@SerializedName(value = channel_id)open override val [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/channelType/#/PointingToDeclaration/"></a>[channelType](channelType.md)| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/channelType/#/PointingToDeclaration/"></a>@SerializedName(value = channel_type)open override val [channelType](channelType.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/cid/#/PointingToDeclaration/"></a>open override val [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/createdAt/#/PointingToDeclaration/"></a>@SerializedName(value = created_at)open override val [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)|
| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/type/#/PointingToDeclaration/"></a>open override val [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/user/#/PointingToDeclaration/"></a>[user](user.md)| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/user/#/PointingToDeclaration/"></a>open override val [user](user.md): [User](../../io.getstream.chat.android.client.models/User/index.md)|
| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/watcherCount/#/PointingToDeclaration/"></a>[watcherCount](watcherCount.md)| <a name="io.getstream.chat.android.client.events/UserStartWatchingEvent/watcherCount/#/PointingToDeclaration/"></a>@SerializedName(value = watcher_count)open override val [watcherCount](watcherCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0|

