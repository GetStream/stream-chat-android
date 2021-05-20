---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.api.models](../index.md)/[QueryChannelsRequest](index.md)  
  
  
  
# QueryChannelsRequest  
class [QueryChannelsRequest](index.md)(**filter**: [FilterObject](../FilterObject/index.md), **offset**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **limit**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **querySort**: [QuerySort](../QuerySort/index.md)&lt;[Channel](../../io.getstream.chat.android.client.models/Channel/index.md)&gt;, **messageLimit**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **memberLimit**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : ChannelRequest&lt;[QueryChannelsRequest](index.md)&gt;   
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/filter/#/PointingToDeclaration/"></a>[filter](filter.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/filter/#/PointingToDeclaration/"></a>@SerializedName(value = filter_conditions)val [filter](filter.md): [FilterObject](../FilterObject/index.md)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/limit/#/PointingToDeclaration/"></a>[limit](limit.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/limit/#/PointingToDeclaration/"></a>var [limit](limit.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/memberLimit/#/PointingToDeclaration/"></a>[memberLimit](memberLimit.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/memberLimit/#/PointingToDeclaration/"></a>@SerializedName(value = member_limit)var [memberLimit](memberLimit.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/messageLimit/#/PointingToDeclaration/"></a>[messageLimit](messageLimit.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/messageLimit/#/PointingToDeclaration/"></a>@SerializedName(value = message_limit)var [messageLimit](messageLimit.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/offset/#/PointingToDeclaration/"></a>[offset](offset.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/offset/#/PointingToDeclaration/"></a>var [offset](offset.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/presence/#/PointingToDeclaration/"></a>[presence](presence.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/presence/#/PointingToDeclaration/"></a>open override var [presence](presence.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/querySort/#/PointingToDeclaration/"></a>[querySort](querySort.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/querySort/#/PointingToDeclaration/"></a>val [querySort](querySort.md): [QuerySort](../QuerySort/index.md)&lt;[Channel](../../io.getstream.chat.android.client.models/Channel/index.md)&gt;|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/sort/#/PointingToDeclaration/"></a>[sort](sort.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/sort/#/PointingToDeclaration/"></a>val [sort](sort.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;&gt;|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/state/#/PointingToDeclaration/"></a>[state](state.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/state/#/PointingToDeclaration/"></a>open override var [state](state.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/watch/#/PointingToDeclaration/"></a>[watch](watch.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/watch/#/PointingToDeclaration/"></a>open override var [watch](watch.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/withLimit/#kotlin.Int/PointingToDeclaration/"></a>[withLimit](withLimit.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/withLimit/#kotlin.Int/PointingToDeclaration/"></a>fun [withLimit](withLimit.md)(limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [QueryChannelsRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/withMessages/#kotlin.Int/PointingToDeclaration/"></a>[withMessages](withMessages.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/withMessages/#kotlin.Int/PointingToDeclaration/"></a>fun [withMessages](withMessages.md)(limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [QueryChannelsRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/withOffset/#kotlin.Int/PointingToDeclaration/"></a>[withOffset](withOffset.md)| <a name="io.getstream.chat.android.client.api.models/QueryChannelsRequest/withOffset/#kotlin.Int/PointingToDeclaration/"></a>fun [withOffset](withOffset.md)(offset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [QueryChannelsRequest](index.md)|
  
  
## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noPresence/#/PointingToDeclaration/"></a>[noPresence](../WatchChannelRequest/index.md#1585137569%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noPresence/#/PointingToDeclaration/"></a>open fun [noPresence](../WatchChannelRequest/index.md#1585137569%2FFunctions%2F-423410878)(): [QueryChannelsRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noState/#/PointingToDeclaration/"></a>[noState](../WatchChannelRequest/index.md#1393237037%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noState/#/PointingToDeclaration/"></a>open fun [noState](../WatchChannelRequest/index.md#1393237037%2FFunctions%2F-423410878)(): [QueryChannelsRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noWatch/#/PointingToDeclaration/"></a>[noWatch](../WatchChannelRequest/index.md#1534934447%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/noWatch/#/PointingToDeclaration/"></a>open fun [noWatch](../WatchChannelRequest/index.md#1534934447%2FFunctions%2F-423410878)(): [QueryChannelsRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withPresence/#/PointingToDeclaration/"></a>[withPresence](../WatchChannelRequest/index.md#-1504229060%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withPresence/#/PointingToDeclaration/"></a>open fun [withPresence](../WatchChannelRequest/index.md#-1504229060%2FFunctions%2F-423410878)(): [QueryChannelsRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withState/#/PointingToDeclaration/"></a>[withState](../WatchChannelRequest/index.md#-2127497102%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withState/#/PointingToDeclaration/"></a>open fun [withState](../WatchChannelRequest/index.md#-2127497102%2FFunctions%2F-423410878)(): [QueryChannelsRequest](index.md)|
| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withWatch/#/PointingToDeclaration/"></a>[withWatch](../WatchChannelRequest/index.md#-1985799692%2FFunctions%2F-423410878)| <a name="io.getstream.chat.android.client.api.models/ChannelRequest/withWatch/#/PointingToDeclaration/"></a>open fun [withWatch](../WatchChannelRequest/index.md#-1985799692%2FFunctions%2F-423410878)(): [QueryChannelsRequest](index.md)|

