---
title: setUserWithoutConnecting
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client](../index.md)/[ChatClient](index.md)/[setUserWithoutConnecting](setUserWithoutConnecting.md)  
  
  
  
# setUserWithoutConnecting  
fun [setUserWithoutConnecting](setUserWithoutConnecting.md)(user: [User](../../io.getstream.chat.android.client.models/User/index.md), userToken: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))Initializes [ChatClient](index.md) for a specific user and a given [userToken](setUserWithoutConnecting.md). Caution: This method doesn't establish connection to the web socket, you should use [connectUser](connectUser.md) instead.This method initializes [ChatClient](index.md) to allow the use of Stream REST API client. Moreover, it warms up the connection, and sets up notifications.  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/setUserWithoutConnecting/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>user| <a name="io.getstream.chat.android.client/ChatClient/setUserWithoutConnecting/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>the user to set|
| <a name="io.getstream.chat.android.client/ChatClient/setUserWithoutConnecting/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>userToken| <a name="io.getstream.chat.android.client/ChatClient/setUserWithoutConnecting/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>the user token|
  

