package io.getstream.chat.android.pushprovider.huawei

import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider
import kotlin.jvm.Throws

internal class ChatHuaweiMessagingService : HmsMessageService() {
    private val logger = ChatLogger.get("ChatHuaweiMessagingService")

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

    override fun onNewToken(token: String?) {
        try {
            token?.takeUnless { it.isNullOrBlank() }
                ?.run {
                    ChatClient.setDevice(
                        Device(
                            token = token,
                            pushProvider = PushProvider.HUAWEI,
                        )
                    )
                }
                ?: logger.logI("Empty token received from Huawei Push Kit")
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
        channelId = dataOfMap["channel_id"]!!,
        messageId = dataOfMap["message_id"]!!,
        channelType = dataOfMap["channel_type"]!!,
    )
    else -> throw IllegalStateException("RemoteMessage doesn't contains needed data")
}

private fun RemoteMessage.isValid() =
    !dataOfMap["channel_id"].isNullOrBlank() &&
        !dataOfMap["message_id"].isNullOrBlank() &&
        !dataOfMap["channel_type"].isNullOrBlank()
