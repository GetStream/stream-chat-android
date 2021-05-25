---
title: handleRemoteMessage
---
//[stream-chat-android-client](../../../../index.md)/[io.getstream.chat.android.client](../../index.md)/[ChatClient](../index.md)/[Companion](index.md)/[handleRemoteMessage](handleRemoteMessage.md)



# handleRemoteMessage  
[androidJvm]  
Content  
fun [handleRemoteMessage](handleRemoteMessage.md)(remoteMessage: RemoteMessage)  
More info  


Handles remote message. If user is not connected - automatically restores last user credentials and sets user without connecting to the socket. Remote message will be handled internally unless user overrides [ChatNotificationHandler.onFirebaseMessage](../../../io.getstream.chat.android.client.notifications.handler/ChatNotificationHandler/onFirebaseMessage.md) Be sure to initialize ChatClient before calling this method!



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient.Companion/handleRemoteMessage/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>[io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler](../../../io.getstream.chat.android.client.notifications.handler/ChatNotificationHandler/onFirebaseMessage.md)| <a name="io.getstream.chat.android.client/ChatClient.Companion/handleRemoteMessage/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>|
  


#### Throws  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient.Companion/handleRemoteMessage/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>[kotlin.IllegalStateException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-illegal-state-exception/index.html)| <a name="io.getstream.chat.android.client/ChatClient.Companion/handleRemoteMessage/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br/><br/>if called before initializing ChatClient<br/><br/>|
  



