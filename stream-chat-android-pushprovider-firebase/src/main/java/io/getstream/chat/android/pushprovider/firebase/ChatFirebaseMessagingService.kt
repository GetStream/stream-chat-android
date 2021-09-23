package io.getstream.chat.android.pushprovider.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.logger.ChatLogger

internal class ChatFirebaseMessagingService : FirebaseMessagingService() {
    private val logger = ChatLogger.get("ChatFirebaseMessagingService")

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.logD("onMessageReceived(): $remoteMessage")
        try {
            FirebaseMessagingDelegate.handleRemoteMessage(remoteMessage)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while handling remote message", exception)
        } finally {
            stopSelf()
        }
    }

    override fun onNewToken(token: String) {
        try {
            FirebaseMessagingDelegate.registerFirebaseToken(token)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while registering Firebase Token", exception)
        }
    }

    private companion object {
        private const val TAG = "Chat:"
    }
}
