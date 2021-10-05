package io.getstream.chat.android.pushprovider.xiaomi

import android.content.Context
import android.util.Log
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import com.xiaomi.mipush.sdk.PushMessageReceiver
import io.getstream.chat.android.client.logger.ChatLogger
import java.lang.IllegalStateException

public class ChatXiaomiMessagingReceiver : PushMessageReceiver() {
    private val logger = ChatLogger.get("ChatXiaomiMessagingReceiver")

    override fun onReceivePassThroughMessage(context: Context, miPushMessage: MiPushMessage) {
        logger.logD("onReceivePassThroughMessage(): $miPushMessage")
        try {
            XiaomiMessagingDelegate.handleRemoteMessage(miPushMessage)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while handling remote message", exception)
        }
    }

    override fun onReceiveRegisterResult(context: Context, miPushCommandMessage: MiPushCommandMessage) {
        try {
            XiaomiMessagingDelegate.registerXiaomiToken(miPushCommandMessage)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while registering Xiaomi Token", exception)
        }
    }

    private companion object {
        private const val TAG = "Chat:"
    }
}
