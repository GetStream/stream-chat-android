---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.controller](../index.md)/[QueryChannelsController](index.md)



# QueryChannelsController  
 [androidJvm] interface [QueryChannelsController](index.md)

The QueryChannelsController is a small helper to show a list of channels

<ul><li>.channels a livedata object with the list of channels. this list</li><li>.loading if we're currently loading</li><li>.loadingMore if we're currently loading more channels</li></ul>   


## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController.ChannelsState///PointingToDeclaration/"></a>[ChannelsState](ChannelsState/index.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController.ChannelsState///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>sealed class [ChannelsState](ChannelsState/index.md)  <br/><br/><br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/channels/#/PointingToDeclaration/"></a>[channels](channels.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/channels/#/PointingToDeclaration/"></a> [androidJvm] abstract val [channels](channels.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Channel&gt;&gt;The list of channels Typically we recommend using [channelsState](channelsState.md) instead, it's a bit more complex but ensures that you're handling all edge cases   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/channelsState/#/PointingToDeclaration/"></a>[channelsState](channelsState.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/channelsState/#/PointingToDeclaration/"></a> [androidJvm] abstract val [channelsState](channelsState.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[QueryChannelsController.ChannelsState](ChannelsState/index.md)&gt;Similar to the channels field, but returns the a ChannelsState object This sealed class makes it easier to verify that you've implemented all possible error/no result states   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/endOfChannels/#/PointingToDeclaration/"></a>[endOfChannels](endOfChannels.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/endOfChannels/#/PointingToDeclaration/"></a> [androidJvm] abstract val [endOfChannels](endOfChannels.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;If we've reached the end of the channels   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/filter/#/PointingToDeclaration/"></a>[filter](filter.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/filter/#/PointingToDeclaration/"></a> [androidJvm] abstract val [filter](filter.md): FilterObjectThe filter used for this query   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/loading/#/PointingToDeclaration/"></a>[loading](loading.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/loading/#/PointingToDeclaration/"></a> [androidJvm] abstract val [loading](loading.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;If we are currently loading channels   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/loadingMore/#/PointingToDeclaration/"></a>[loadingMore](loadingMore.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/loadingMore/#/PointingToDeclaration/"></a> [androidJvm] abstract val [loadingMore](loadingMore.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;If we are currently loading more channels   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/mutedChannelIds/#/PointingToDeclaration/"></a>[mutedChannelIds](mutedChannelIds.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/mutedChannelIds/#/PointingToDeclaration/"></a> [androidJvm] abstract val [mutedChannelIds](mutedChannelIds.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;&gt;   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/newChannelEventFilter/#/PointingToDeclaration/"></a>[newChannelEventFilter](newChannelEventFilter.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/newChannelEventFilter/#/PointingToDeclaration/"></a> [androidJvm] abstract var [newChannelEventFilter](newChannelEventFilter.md): (Channel, FilterObject) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)When the NotificationAddedToChannelEvent is triggered the newChannelEventFilter determines if the channel should be added to the query or not.   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/recoveryNeeded/#/PointingToDeclaration/"></a>[recoveryNeeded](recoveryNeeded.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/recoveryNeeded/#/PointingToDeclaration/"></a> [androidJvm] abstract val [recoveryNeeded](recoveryNeeded.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)If the API call failed and we need to rerun this query   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/sort/#/PointingToDeclaration/"></a>[sort](sort.md)| <a name="io.getstream.chat.android.livedata.controller/QueryChannelsController/sort/#/PointingToDeclaration/"></a> [androidJvm] abstract val [sort](sort.md): QuerySort&lt;Channel&gt;The sort used for this query   <br/>|

