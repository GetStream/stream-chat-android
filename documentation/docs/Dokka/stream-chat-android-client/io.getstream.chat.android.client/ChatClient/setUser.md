---
title: setUser
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client](../index.md)/[ChatClient](index.md)/[setUser](setUser.md)  
  
  
  
# setUser  
~~fun~~ [~~setUser~~](setUser.md)~~(~~~~user~~~~:~~ [User](../../io.getstream.chat.android.client.models/User/index.md)~~,~~ ~~token~~~~:~~ [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)~~,~~ ~~listener~~~~:~~ [InitConnectionListener](../../io.getstream.chat.android.client.socket/InitConnectionListener/index.md)? ~~= null~~~~)~~Initializes [ChatClient](index.md) for a specific user using the given user [token](setUser.md).  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/setUser/#io.getstream.chat.android.client.models.User#kotlin.String#io.getstream.chat.android.client.socket.InitConnectionListener?/PointingToDeclaration/"></a>[io.getstream.chat.android.client.ChatClient](setUser.md)| <a name="io.getstream.chat.android.client/ChatClient/setUser/#io.getstream.chat.android.client.models.User#kotlin.String#io.getstream.chat.android.client.socket.InitConnectionListener?/PointingToDeclaration/"></a>with [TokenProvider](../../io.getstream.chat.android.client.token/TokenProvider/index.md) for advanced use cases|
  
~~fun~~ [~~setUser~~](setUser.md)~~(~~~~user~~~~:~~ [User](../../io.getstream.chat.android.client.models/User/index.md)~~,~~ ~~tokenProvider~~~~:~~ [TokenProvider](../../io.getstream.chat.android.client.token/TokenProvider/index.md)~~,~~ ~~listener~~~~:~~ [InitConnectionListener](../../io.getstream.chat.android.client.socket/InitConnectionListener/index.md)? ~~= null~~~~)~~Initializes [ChatClient](index.md) for a specific user. The [tokenProvider](setUser.md) implementation is used for the initial token, and it's also invoked whenever the user's token has expired, to fetch a new token.This method performs required operations before connecting with the Stream API. Moreover, it warms up the connection, sets up notifications, and connects to the socket. You can use [listener](setUser.md) to get updates about socket connection.  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/setUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider#io.getstream.chat.android.client.socket.InitConnectionListener?/PointingToDeclaration/"></a>user| <a name="io.getstream.chat.android.client/ChatClient/setUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider#io.getstream.chat.android.client.socket.InitConnectionListener?/PointingToDeclaration/"></a>the user to set|
| <a name="io.getstream.chat.android.client/ChatClient/setUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider#io.getstream.chat.android.client.socket.InitConnectionListener?/PointingToDeclaration/"></a>tokenProvider| <a name="io.getstream.chat.android.client/ChatClient/setUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider#io.getstream.chat.android.client.socket.InitConnectionListener?/PointingToDeclaration/"></a>a [TokenProvider](../../io.getstream.chat.android.client.token/TokenProvider/index.md) implementation|
| <a name="io.getstream.chat.android.client/ChatClient/setUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider#io.getstream.chat.android.client.socket.InitConnectionListener?/PointingToDeclaration/"></a>listener| <a name="io.getstream.chat.android.client/ChatClient/setUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider#io.getstream.chat.android.client.socket.InitConnectionListener?/PointingToDeclaration/"></a>socket connection listener|
  

