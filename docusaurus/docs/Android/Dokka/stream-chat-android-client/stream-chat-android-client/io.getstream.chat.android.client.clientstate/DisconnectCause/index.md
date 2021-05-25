---
title: index
sidebar_position: 1
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client.clientstate](../index.md)/[DisconnectCause](index.md)



# DisconnectCause  
 [androidJvm] sealed class [DisconnectCause](index.md)

Sealed class represents possible cause of disconnection.

   


## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.ConnectionReleased///PointingToDeclaration/"></a>[ConnectionReleased](ConnectionReleased/index.md)| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.ConnectionReleased///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [ConnectionReleased](ConnectionReleased/index.md) : [DisconnectCause](index.md)  <br/>More info  <br/>Happens when disconnection has been done intentionally.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.Error///PointingToDeclaration/"></a>[Error](Error/index.md)| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.Error///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>class [Error](Error/index.md)(**error**: [ChatNetworkError](../../io.getstream.chat.android.client.errors/ChatNetworkError/index.md)?) : [DisconnectCause](index.md)  <br/>More info  <br/>Happens when some non critical error occurs.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.NetworkNotAvailable///PointingToDeclaration/"></a>[NetworkNotAvailable](NetworkNotAvailable/index.md)| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.NetworkNotAvailable///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [NetworkNotAvailable](NetworkNotAvailable/index.md) : [DisconnectCause](index.md)  <br/>More info  <br/>Happens when networks is not available anymore.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.UnrecoverableError///PointingToDeclaration/"></a>[UnrecoverableError](UnrecoverableError/index.md)| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.UnrecoverableError///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>class [UnrecoverableError](UnrecoverableError/index.md)(**error**: [ChatNetworkError](../../io.getstream.chat.android.client.errors/ChatNetworkError/index.md)?) : [DisconnectCause](index.md)  <br/>More info  <br/>Happens when a critical error occurs.  <br/><br/><br/>|


## Inheritors  
  
|  Name | 
|---|
| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.NetworkNotAvailable///PointingToDeclaration/"></a>[DisconnectCause](NetworkNotAvailable/index.md)|
| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.Error///PointingToDeclaration/"></a>[DisconnectCause](Error/index.md)|
| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.UnrecoverableError///PointingToDeclaration/"></a>[DisconnectCause](UnrecoverableError/index.md)|
| <a name="io.getstream.chat.android.client.clientstate/DisconnectCause.ConnectionReleased///PointingToDeclaration/"></a>[DisconnectCause](ConnectionReleased/index.md)|

