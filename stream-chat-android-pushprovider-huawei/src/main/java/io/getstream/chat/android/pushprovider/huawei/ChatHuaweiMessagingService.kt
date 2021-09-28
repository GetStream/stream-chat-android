package io.getstream.chat.android.pushprovider.huawei

import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import io.getstream.chat.android.client.logger.ChatLogger

internal class ChatHuaweiMessagingService : HmsMessageService() {
    private val logger = ChatLogger.get("ChatHuaweiMessagingService")

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.logD("onMessageReceived(): $remoteMessage")
        try {
            HuaweiMessagingDelegate.handleRemoteMessage(remoteMessage)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while handling remote message", exception)
        } finally {
            stopSelf()
        }
    }

    override fun onNewToken(token: String) {
        try {
            HuaweiMessagingDelegate.registerHuaweiToken(token)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while registering Huawei Token", exception)
        }
    }

    private companion object {
        private const val TAG = "Chat:"
    }
}
