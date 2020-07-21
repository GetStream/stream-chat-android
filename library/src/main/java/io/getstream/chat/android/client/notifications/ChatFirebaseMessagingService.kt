package io.getstream.chat.android.client.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger

class ChatFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = ChatFirebaseMessagingService::class.java.simpleName

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        ChatLogger.instance.logI(TAG, "onMessageReceived: ${remoteMessage.data}")
        ChatClient.instance().onMessageReceived(remoteMessage, this)
    }

    override fun onNewToken(token: String) {
        ChatLogger.instance.logI(TAG, "onNewToken: $token")
        ChatClient.instance().onNewTokenReceived(token, this)
    }
}
