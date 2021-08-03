package io.getstream.chat.android.client.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider

internal class ChatFirebaseMessagingService : FirebaseMessagingService() {
    private val logger = ChatLogger.get("ChatFirebaseMessagingService")

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.logD("onMessageReceived(): $remoteMessage")
        try {
            ChatClient.handleRemoteMessage(remoteMessage)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while handling remote message: ${exception.message}")
        } finally {
            stopSelf()
        }
    }

    override fun onNewToken(token: String) {
        try {
            ChatClient.setDevice(
                Device(
                    token = token,
                    pushProvider = PushProvider.FIREBASE,
                )
            )
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while handling remote message: ${exception.message}")
        }
    }

    private companion object {
        private const val TAG = "Chat:"
    }
}
