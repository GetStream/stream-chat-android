package io.getstream.chat.android.pushprovider.firebase

import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider

/**
 * Helper class for delegating Firebase push messages to the Stream Chat SDK.
 */
public object FirebaseMessagingDelegate {

    /**
     * Handles [remoteMessage] from Firebase.
     * If the [remoteMessage] wasn't sent from the Stream Server and doesn't contain the needed data,
     * return false to notify you that this remoteMessage needs to be handled by you.
     *
     * @param remoteMessage The message to be handled.
     * @return True if the [remoteMessage] was sent from the Stream Server and has been handled.
     *
     * @throws IllegalStateException If called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun handleRemoteMessage(remoteMessage: RemoteMessage): Boolean {
        if (!remoteMessage.isValid()) {
            return false
        }

        ChatClient.handlePushMessage(remoteMessage.toPushMessage())
        return true
    }

    /**
     * Register new Firebase Token.
     *
     * @param token provided by Firebase.
     *
     * @throws IllegalStateException if called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun registerFirebaseToken(token: String) {
        ChatClient.setDevice(
            Device(
                token = token,
                pushProvider = PushProvider.FIREBASE,
            )
        )
    }
}

private fun RemoteMessage.toPushMessage() =
    PushMessage(
        channelId = data.getValue("channel_id"),
        messageId = data.getValue("message_id"),
        channelType = data.getValue("channel_type"),
    )

private fun RemoteMessage.isValid() =
    !data["channel_id"].isNullOrBlank() &&
        !data["message_id"].isNullOrBlank() &&
        !data["channel_type"].isNullOrBlank()
