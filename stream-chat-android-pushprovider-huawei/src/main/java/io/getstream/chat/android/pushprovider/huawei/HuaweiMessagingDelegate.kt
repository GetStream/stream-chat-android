package io.getstream.chat.android.pushprovider.huawei

import com.huawei.hms.push.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider
import kotlin.jvm.Throws

/**
 * Helper class for delegating Huawei push messages to the Stream Chat SDK.
 */
public object HuaweiMessagingDelegate {

    /**
     * Handles [remoteMessage] from Huawei.
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
     * Register new Huawei Token.
     *
     * @param token provided by Huawei.
     *
     * @throws IllegalStateException if called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun registerHuaweiToken(token: String) {
        ChatClient.setDevice(
            Device(
                token = token,
                pushProvider = PushProvider.HUAWEI,
            )
        )
    }
}

private fun RemoteMessage.toPushMessage() =
    PushMessage(
        channelId = dataOfMap["channel_id"]!!,
        messageId = dataOfMap["message_id"]!!,
        channelType = dataOfMap["channel_type"]!!,
    )

private fun RemoteMessage.isValid() =
    !dataOfMap["channel_id"].isNullOrBlank() &&
        !dataOfMap["message_id"].isNullOrBlank() &&
        !dataOfMap["channel_type"].isNullOrBlank()
