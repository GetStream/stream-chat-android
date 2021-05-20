---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.api.models](../index.md)/[QueryChannelRequest](index.md)  
  
  
  
# QueryChannelRequest  
open class [QueryChannelRequest](index.md) : ChannelRequest&lt;[QueryChannelRequest](index.md)&gt;   
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/data/#/PointingToDeclaration/"></a>[data](data.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/data/#/PointingToDeclaration/"></a>val [data](data.md): [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/members/#/PointingToDeclaration/"></a>[members](members.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/members/#/PointingToDeclaration/"></a>val [members](members.md): [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/messages/#/PointingToDeclaration/"></a>[messages](messages.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/messages/#/PointingToDeclaration/"></a>val [messages](messages.md): [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/presence/#/PointingToDeclaration/"></a>[presence](presence.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/presence/#/PointingToDeclaration/"></a>open override var [presence](presence.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/state/#/PointingToDeclaration/"></a>[state](state.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/state/#/PointingToDeclaration/"></a>open override var [state](state.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/watch/#/PointingToDeclaration/"></a>[watch](watch.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/watch/#/PointingToDeclaration/"></a>open override var [watch](watch.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/watchers/#/PointingToDeclaration/"></a>[watchers](watchers.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/watchers/#/PointingToDeclaration/"></a>val [watchers](watchers.md): [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/filteringOlderMessages/#/PointingToDeclaration/"></a>[filteringOlderMessages](filteringOlderMessages.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/filteringOlderMessages/#/PointingToDeclaration/"></a>fun [filteringOlderMessages](filteringOlderMessages.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/isFilteringNewerMessages/#/PointingToDeclaration/"></a>[isFilteringNewerMessages](isFilteringNewerMessages.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/isFilteringNewerMessages/#/PointingToDeclaration/"></a>fun [isFilteringNewerMessages](isFilteringNewerMessages.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/withData/#kotlin.collections.Map[kotlin.String,kotlin.Any]/PointingToDeclaration/"></a>[withData](withData.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/withData/#kotlin.collections.Map[kotlin.String,kotlin.Any]/PointingToDeclaration/"></a>open fun [withData](withData.md)(data: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;): [QueryChannelRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/withMembers/#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[withMembers](withMembers.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/withMembers/#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>open fun [withMembers](withMembers.md)(limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), offset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [QueryChannelRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/withMessages/#kotlin.Int/PointingToDeclaration/"></a>[withMessages](withMessages.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/withMessages/#kotlin.Int/PointingToDeclaration/"></a>open fun [withMessages](withMessages.md)(limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [QueryChannelRequest](index.md)open fun [withMessages](withMessages.md)(direction: [Pagination](../Pagination/index.md), messageId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [QueryChannelRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/withWatchers/#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[withWatchers](withWatchers.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelRequest/withWatchers/#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>open fun [withWatchers](withWatchers.md)(limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), offset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [QueryChannelRequest](index.md)|
  
  
## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noPresence/#/PointingToDeclaration/"></a>[noPresence](../WatchChannelRequest/index.md#1585137569%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noPresence/#/PointingToDeclaration/"></a>open fun [noPresence](../WatchChannelRequest/index.md#1585137569%2FFunctions%2F-423410878)(): [QueryChannelRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noState/#/PointingToDeclaration/"></a>[noState](../WatchChannelRequest/index.md#1393237037%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noState/#/PointingToDeclaration/"></a>open fun [noState](../WatchChannelRequest/index.md#1393237037%2FFunctions%2F-423410878)(): [QueryChannelRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noWatch/#/PointingToDeclaration/"></a>[noWatch](../WatchChannelRequest/index.md#1534934447%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noWatch/#/PointingToDeclaration/"></a>open fun [noWatch](../WatchChannelRequest/index.md#1534934447%2FFunctions%2F-423410878)(): [QueryChannelRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withPresence/#/PointingToDeclaration/"></a>[withPresence](../WatchChannelRequest/index.md#-1504229060%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withPresence/#/PointingToDeclaration/"></a>open fun [withPresence](../WatchChannelRequest/index.md#-1504229060%2FFunctions%2F-423410878)(): [QueryChannelRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withState/#/PointingToDeclaration/"></a>[withState](../WatchChannelRequest/index.md#-2127497102%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withState/#/PointingToDeclaration/"></a>open fun [withState](../WatchChannelRequest/index.md#-2127497102%2FFunctions%2F-423410878)(): [QueryChannelRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withWatch/#/PointingToDeclaration/"></a>[withWatch](../WatchChannelRequest/index.md#-1985799692%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withWatch/#/PointingToDeclaration/"></a>open fun [withWatch](../WatchChannelRequest/index.md#-1985799692%2FFunctions%2F-423410878)(): [QueryChannelRequest](index.md)|
  
  
## Inheritors  
  
|  Name | 
|---|
| <a name="io.getstream.chat.android.client.api.models/WatchChannelRequest///PointingToDeclaration/"></a>[WatchChannelRequest](../WatchChannelRequest/index.md)|

