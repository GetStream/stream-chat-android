---
title: index
sidebar_position: 1
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChannelData](index.md)  
  
  
  
# ChannelData  
~~data~~ ~~class~~ [~~ChannelData~~](index.md)A class that only stores the channel data and not all the other channel state Using this prevents code bugs and issues caused by confusing the channel data vs the full channel object  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChannelData/ChannelData/#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User#kotlin.Int#kotlin.Boolean#java.util.Date?#java.util.Date?#java.util.Date?#kotlin.Int#kotlin.String#kotlin.collections.MutableMap[kotlin.String,kotlin.Any]/PointingToDeclaration/"></a>[ChannelData](ChannelData.md)| <a name="io.getstream.chat.android.livedata/ChannelData/ChannelData/#kotlin.String#kotlin.String#kotlin.String#io.getstream.chat.android.client.models.User#kotlin.Int#kotlin.Boolean#java.util.Date?#java.util.Date?#java.util.Date?#kotlin.Int#kotlin.String#kotlin.collections.MutableMap[kotlin.String,kotlin.Any]/PointingToDeclaration/"></a>fun [ChannelData](ChannelData.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "%s:%s".format(type, channelId), createdBy: User = User(), cooldown: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, frozen: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null, updatedAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null, deletedAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null, memberCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, team: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "", extraData: [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; = mutableMapOf())|
| <a name="io.getstream.chat.android.livedata/ChannelData/ChannelData/#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a>[ChannelData](ChannelData.md)| <a name="io.getstream.chat.android.livedata/ChannelData/ChannelData/#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a>fun [ChannelData](ChannelData.md)(c: Channel)create a ChannelData object from a Channel object|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata/ChannelData/channelId/#/PointingToDeclaration/"></a>[channelId](channelId.md)| <a name="io.getstream.chat.android.livedata/ChannelData/channelId/#/PointingToDeclaration/"></a>var [channelId](channelId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.livedata/ChannelData/cid/#/PointingToDeclaration/"></a>[cid](cid.md)| <a name="io.getstream.chat.android.livedata/ChannelData/cid/#/PointingToDeclaration/"></a>var [cid](cid.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.livedata/ChannelData/cooldown/#/PointingToDeclaration/"></a>[cooldown](cooldown.md)| <a name="io.getstream.chat.android.livedata/ChannelData/cooldown/#/PointingToDeclaration/"></a>var [cooldown](cooldown.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)|
| <a name="io.getstream.chat.android.livedata/ChannelData/createdAt/#/PointingToDeclaration/"></a>[createdAt](createdAt.md)| <a name="io.getstream.chat.android.livedata/ChannelData/createdAt/#/PointingToDeclaration/"></a>var [createdAt](createdAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?|
| <a name="io.getstream.chat.android.livedata/ChannelData/createdBy/#/PointingToDeclaration/"></a>[createdBy](createdBy.md)| <a name="io.getstream.chat.android.livedata/ChannelData/createdBy/#/PointingToDeclaration/"></a>var [createdBy](createdBy.md): User|
| <a name="io.getstream.chat.android.livedata/ChannelData/deletedAt/#/PointingToDeclaration/"></a>[deletedAt](deletedAt.md)| <a name="io.getstream.chat.android.livedata/ChannelData/deletedAt/#/PointingToDeclaration/"></a>var [deletedAt](deletedAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?|
| <a name="io.getstream.chat.android.livedata/ChannelData/extraData/#/PointingToDeclaration/"></a>[extraData](extraData.md)| <a name="io.getstream.chat.android.livedata/ChannelData/extraData/#/PointingToDeclaration/"></a>var [extraData](extraData.md): [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;|
| <a name="io.getstream.chat.android.livedata/ChannelData/frozen/#/PointingToDeclaration/"></a>[frozen](frozen.md)| <a name="io.getstream.chat.android.livedata/ChannelData/frozen/#/PointingToDeclaration/"></a>var [frozen](frozen.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)|
| <a name="io.getstream.chat.android.livedata/ChannelData/memberCount/#/PointingToDeclaration/"></a>[memberCount](memberCount.md)| <a name="io.getstream.chat.android.livedata/ChannelData/memberCount/#/PointingToDeclaration/"></a>var [memberCount](memberCount.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)|
| <a name="io.getstream.chat.android.livedata/ChannelData/team/#/PointingToDeclaration/"></a>[team](team.md)| <a name="io.getstream.chat.android.livedata/ChannelData/team/#/PointingToDeclaration/"></a>var [team](team.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.livedata/ChannelData/type/#/PointingToDeclaration/"></a>[type](type.md)| <a name="io.getstream.chat.android.livedata/ChannelData/type/#/PointingToDeclaration/"></a>var [type](type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.livedata/ChannelData/updatedAt/#/PointingToDeclaration/"></a>[updatedAt](updatedAt.md)| <a name="io.getstream.chat.android.livedata/ChannelData/updatedAt/#/PointingToDeclaration/"></a>var [updatedAt](updatedAt.md): [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)?|

