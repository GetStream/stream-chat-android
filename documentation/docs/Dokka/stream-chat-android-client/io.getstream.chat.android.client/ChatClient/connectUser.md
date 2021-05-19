---
title: connectUser
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client](../index.md)/[ChatClient](index.md)/[connectUser](connectUser.md)  
  
  
  
# connectUser  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()fun [connectUser](connectUser.md)(user: [User](../../io.getstream.chat.android.client.models/User/index.md), token: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): Call&lt;[ConnectionData](../../io.getstream.chat.android.client.models/ConnectionData/index.md)&gt;Initializes [ChatClient](index.md) for a specific user using the given user [token](connectUser.md).  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>[io.getstream.chat.android.client.ChatClient](connectUser.md)| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>with [TokenProvider](../../io.getstream.chat.android.client.token/TokenProvider/index.md) parameter for advanced use cases.|
  
  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>user| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>Instance of [User](../../io.getstream.chat.android.client.models/User/index.md) type.|
| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>token| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#kotlin.String/PointingToDeclaration/"></a>Instance of JWT token. It must be unique for each user. Check out [docs](https://getstream.io/chat/docs/android/init_and_users/) for more info about tokens. Also visit [this site](https://jwt.io) to find more about Json Web Token standard. You can generate the JWT token on using one of the available libraries or use our manual [tool](https://getstream.io/chat/docs/react/token_generator/) for token generation.|
  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()fun [connectUser](connectUser.md)(user: [User](../../io.getstream.chat.android.client.models/User/index.md), tokenProvider: [TokenProvider](../../io.getstream.chat.android.client.token/TokenProvider/index.md)): Call&lt;[ConnectionData](../../io.getstream.chat.android.client.models/ConnectionData/index.md)&gt;Initializes [ChatClient](index.md) for a specific user. The [tokenProvider](connectUser.md) implementation is used for the initial token, and it's also invoked whenever the user's token has expired, to fetch a new token.This method performs required operations before connecting with the Stream API. Moreover, it warms up the connection, sets up notifications, and connects to the socket. You can use listener to get updates about socket connection.  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider/PointingToDeclaration/"></a>user| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider/PointingToDeclaration/"></a>the user to set|
| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider/PointingToDeclaration/"></a>tokenProvider| <a name="io.getstream.chat.android.client/ChatClient/connectUser/#io.getstream.chat.android.client.models.User#io.getstream.chat.android.client.token.TokenProvider/PointingToDeclaration/"></a>a [TokenProvider](../../io.getstream.chat.android.client.token/TokenProvider/index.md) implementation|
  

