---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../../index.md)/[io.getstream.chat.android.offline.channel](../../index.md)/[ChannelController](../index.md)/[MessagesState](index.md)



# MessagesState  
 [androidJvm] sealed class [MessagesState](index.md)   


## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.Loading///PointingToDeclaration/"></a>[Loading](Loading/index.md)| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.Loading///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [Loading](Loading/index.md) : [ChannelController.MessagesState](index.md)  <br/>More info  <br/>Indicates we are loading the first page of results.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.NoQueryActive///PointingToDeclaration/"></a>[NoQueryActive](NoQueryActive/index.md)| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.NoQueryActive///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [NoQueryActive](NoQueryActive/index.md) : [ChannelController.MessagesState](index.md)  <br/>More info  <br/>The ChannelController is initialized but no query is currently running.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.OfflineNoResults///PointingToDeclaration/"></a>[OfflineNoResults](OfflineNoResults/index.md)| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.OfflineNoResults///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [OfflineNoResults](OfflineNoResults/index.md) : [ChannelController.MessagesState](index.md)  <br/>More info  <br/>If we are offline and don't have channels stored in offline storage, typically displayed as an error condition.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.Result///PointingToDeclaration/"></a>[Result](Result/index.md)| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.Result///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>data class [Result](Result/index.md)(**messages**: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;) : [ChannelController.MessagesState](index.md)  <br/>More info  <br/>The list of messages, loaded either from offline storage or an API call.  <br/><br/><br/>|


## Inheritors  
  
|  Name | 
|---|
| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.NoQueryActive///PointingToDeclaration/"></a>[ChannelController.MessagesState](NoQueryActive/index.md)|
| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.Loading///PointingToDeclaration/"></a>[ChannelController.MessagesState](Loading/index.md)|
| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.OfflineNoResults///PointingToDeclaration/"></a>[ChannelController.MessagesState](OfflineNoResults/index.md)|
| <a name="io.getstream.chat.android.offline.channel/ChannelController.MessagesState.Result///PointingToDeclaration/"></a>[ChannelController.MessagesState](Result/index.md)|

