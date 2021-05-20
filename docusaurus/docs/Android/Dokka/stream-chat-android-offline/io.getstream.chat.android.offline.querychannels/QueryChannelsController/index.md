---
title: index
sidebar_position: 1
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.offline.querychannels](../index.md)/[QueryChannelsController](index.md)  
  
  
  
# QueryChannelsController  
class [QueryChannelsController](index.md)  
  
## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState///PointingToDeclaration/"></a>[ChannelsState](ChannelsState/index.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState///PointingToDeclaration/"></a>sealed class [ChannelsState](ChannelsState/index.md)|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/channels/#/PointingToDeclaration/"></a>[channels](channels.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/channels/#/PointingToDeclaration/"></a>val [channels](channels.md): StateFlow&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Channel&gt;&gt;|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/channelsState/#/PointingToDeclaration/"></a>[channelsState](channelsState.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/channelsState/#/PointingToDeclaration/"></a>val [channelsState](channelsState.md): StateFlow&lt;[QueryChannelsController.ChannelsState](ChannelsState/index.md)&gt;|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/endOfChannels/#/PointingToDeclaration/"></a>[endOfChannels](endOfChannels.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/endOfChannels/#/PointingToDeclaration/"></a>val [endOfChannels](endOfChannels.md): StateFlow&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/filter/#/PointingToDeclaration/"></a>[filter](filter.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/filter/#/PointingToDeclaration/"></a>val [filter](filter.md): FilterObject|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/loading/#/PointingToDeclaration/"></a>[loading](loading.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/loading/#/PointingToDeclaration/"></a>val [loading](loading.md): StateFlow&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/loadingMore/#/PointingToDeclaration/"></a>[loadingMore](loadingMore.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/loadingMore/#/PointingToDeclaration/"></a>val [loadingMore](loadingMore.md): StateFlow&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/mutedChannelIds/#/PointingToDeclaration/"></a>[mutedChannelIds](mutedChannelIds.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/mutedChannelIds/#/PointingToDeclaration/"></a>val [mutedChannelIds](mutedChannelIds.md): StateFlow&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;&gt;|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/newChannelEventFilter/#/PointingToDeclaration/"></a>[newChannelEventFilter](newChannelEventFilter.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/newChannelEventFilter/#/PointingToDeclaration/"></a>var [newChannelEventFilter](newChannelEventFilter.md): (Channel, FilterObject) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/recoveryNeeded/#/PointingToDeclaration/"></a>[recoveryNeeded](recoveryNeeded.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/recoveryNeeded/#/PointingToDeclaration/"></a>var [recoveryNeeded](recoveryNeeded.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/sort/#/PointingToDeclaration/"></a>[sort](sort.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/sort/#/PointingToDeclaration/"></a>val [sort](sort.md): QuerySort&lt;Channel&gt;|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/query/#kotlin.Int#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[query](query.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/query/#kotlin.Int#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>suspend fun [query](query.md)(channelLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = CHANNEL_LIMIT, messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = MESSAGE_LIMIT, memberLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = MEMBER_LIMIT): Result&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Channel&gt;&gt;|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/refreshChannel/#kotlin.String/PointingToDeclaration/"></a>[refreshChannel](refreshChannel.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController/refreshChannel/#kotlin.String/PointingToDeclaration/"></a>fun [refreshChannel](refreshChannel.md)(cId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))refreshes a single channel Note that this only refreshes channels that are already matching with the query It retrieves the data from the current channelController object|

