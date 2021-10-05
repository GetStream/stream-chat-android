package io.getstream.chat.android.pushprovider.xiaomi

import com.xiaomi.mipush.sdk.MiPushClient
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider
import kotlin.jvm.Throws

/**
 * Helper class for delegating Xiaomi push messages to the Stream Chat SDK.
 */
public object XiaomiMessagingDelegate {

    /**
     * Handles [miPushMessage] from Xiaomi.
     * If the [miPushMessage] wasn't sent from the Stream Server and doesn't contain the needed data,
     * return false to notify you that this remoteMessage needs to be handled by you.
     *
     * @param miPushMessage The message to be handled.
     * @return True if the [miPushMessage] was sent from the Stream Server and has been handled.
     *
     * @throws IllegalStateException If called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun handleRemoteMessage(miPushMessage: MiPushMessage): Boolean {
        if (!miPushMessage.isValid()) {
            return false
        }

        ChatClient.handlePushMessage(miPushMessage.toPushMessage())
        return true
    }

    /**
     * Register new Xiaomi Token.
     *
     * @param miPushCommandMessage provided by Xiaomi.
     *
     * @throws IllegalStateException if called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun registerXiaomiToken(miPushCommandMessage: MiPushCommandMessage) {
        miPushCommandMessage
            .takeIf { it.command == MiPushClient.COMMAND_REGISTER }
            ?.commandArguments
            ?.get(0)
            ?.run {
                ChatClient.setDevice(
                    Device(
                        token = this,
                        pushProvider = PushProvider.XIAOMI,
                    )
                )
            }
    }
}

private fun MiPushMessage.toPushMessage() =
    PushMessage(
        channelId = extra["channel_id"]!!,
        messageId = extra["message_id"]!!,
        channelType = extra["channel_type"]!!,
    )

private fun MiPushMessage.isValid() =
    !extra["channel_id"].isNullOrBlank() &&
        !extra["message_id"].isNullOrBlank() &&
        !extra["channel_type"].isNullOrBlank()
