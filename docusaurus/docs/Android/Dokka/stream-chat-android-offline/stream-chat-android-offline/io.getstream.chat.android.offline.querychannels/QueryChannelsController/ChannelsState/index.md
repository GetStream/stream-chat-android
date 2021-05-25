---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../../index.md)/[io.getstream.chat.android.offline.querychannels](../../index.md)/[QueryChannelsController](../index.md)/[ChannelsState](index.md)



# ChannelsState  
 [androidJvm] sealed class [ChannelsState](index.md)   


## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.Loading///PointingToDeclaration/"></a>[Loading](Loading/index.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.Loading///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [Loading](Loading/index.md) : [QueryChannelsController.ChannelsState](index.md)  <br/>More info  <br/>Indicates we are loading the first page of results.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.NoQueryActive///PointingToDeclaration/"></a>[NoQueryActive](NoQueryActive/index.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.NoQueryActive///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [NoQueryActive](NoQueryActive/index.md) : [QueryChannelsController.ChannelsState](index.md)  <br/>More info  <br/>The QueryChannelsController is initialized but no query is currently running.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.OfflineNoResults///PointingToDeclaration/"></a>[OfflineNoResults](OfflineNoResults/index.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.OfflineNoResults///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [OfflineNoResults](OfflineNoResults/index.md) : [QueryChannelsController.ChannelsState](index.md)  <br/>More info  <br/>If we are offline and don't have channels stored in offline storage, typically displayed as an error condition.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.Result///PointingToDeclaration/"></a>[Result](Result/index.md)| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.Result///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>data class [Result](Result/index.md)(**channels**: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Channel&gt;) : [QueryChannelsController.ChannelsState](index.md)  <br/>More info  <br/>The list of channels, loaded either from offline storage or an API call.  <br/><br/><br/>|


## Inheritors  
  
|  Name | 
|---|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.NoQueryActive///PointingToDeclaration/"></a>[QueryChannelsController.ChannelsState](NoQueryActive/index.md)|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.Loading///PointingToDeclaration/"></a>[QueryChannelsController.ChannelsState](Loading/index.md)|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.OfflineNoResults///PointingToDeclaration/"></a>[QueryChannelsController.ChannelsState](OfflineNoResults/index.md)|
| <a name="io.getstream.chat.android.offline.querychannels/QueryChannelsController.ChannelsState.Result///PointingToDeclaration/"></a>[QueryChannelsController.ChannelsState](Result/index.md)|

