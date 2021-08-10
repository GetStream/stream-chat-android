package io.getstream.chat.android.pushprovider.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider
import kotlin.jvm.Throws

internal class ChatFirebaseMessagingService : FirebaseMessagingService() {
    private val logger = ChatLogger.get("ChatFirebaseMessagingService")

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.logD("onMessageReceived(): $remoteMessage")
        try {
            remoteMessage.takeIf { it.isValid() }
                ?.toPushMessage()
                ?.run { ChatClient.handlePushMessage(this) }
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

@Throws(IllegalStateException::class)
private fun RemoteMessage.toPushMessage() = when (isValid()) {
    true -> PushMessage(
        channelId = data["channel_id"]!!,
        messageId = data["message_id"]!!,
        channelType = data["channel_type"]!!,
    )
    else -> throw IllegalStateException("RemoteMessage doesn't contains needed data")
}

private fun RemoteMessage.isValid() =
    !data["channel_id"].isNullOrBlank() &&
        !data["message_id"].isNullOrBlank() &&
        !data["channel_type"].isNullOrBlank()
