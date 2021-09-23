package io.getstream.chat.android.pushprovider.firebase

import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider
import kotlin.jvm.Throws

public object FirebaseMessagingDelegate {

    /**
     * Handles [remoteMessage] from Firebase.
     * If the [remoteMessage] wasn't sent from Stream Server and doesn't contain needed data return false to notify you this remoteMessage need to be handled by you.
     *
     * @param remoteMessage to be handled.
     * @return True if the [remoteMessage] was sent from Stream Server and has been handled internally.
     *
     * @throws IllegalStateException if called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun handleRemoteMessage(remoteMessage: RemoteMessage): Boolean =
        remoteMessage.takeIf { it.isValid() }
            ?.toPushMessage()
            ?.run(ChatClient::handlePushMessage) != null

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
