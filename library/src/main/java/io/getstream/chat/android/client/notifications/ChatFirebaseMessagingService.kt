package io.getstream.chat.android.client.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient

class ChatFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        ChatClient.instance().onMessageReceived(remoteMessage, this)
    }

    override fun onNewToken(token: String) {
        ChatClient.instance().onNewTokenReceived(token, this)
    }
}