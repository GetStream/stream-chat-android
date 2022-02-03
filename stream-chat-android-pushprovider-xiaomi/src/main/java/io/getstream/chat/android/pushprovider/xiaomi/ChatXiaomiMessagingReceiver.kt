package io.getstream.chat.android.pushprovider.xiaomi

import android.content.Context
import android.util.Log
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import com.xiaomi.mipush.sdk.PushMessageReceiver
import io.getstream.chat.android.client.logger.ChatLogger
import java.lang.IllegalStateException

/**
 * Receiver that handle Push Notifications from Xiaomi servers.
 */
public class ChatXiaomiMessagingReceiver : PushMessageReceiver() {
    private val logger = ChatLogger.get("ChatXiaomiMessagingReceiver")

    /**
     * This method is called when a push notification is received from Xiaomi Servers.
     *
     * @param context The [Context] where this code is run.
     * @param miPushMessage A [MiPushMessage] that contains inifo about the push notification.
     */
    override fun onReceivePassThroughMessage(context: Context, miPushMessage: MiPushMessage) {
        logger.logD("onReceivePassThroughMessage(): $miPushMessage")
        try {
            XiaomiMessagingDelegate.handleMiPushMessage(miPushMessage)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while handling remote message", exception)
        }
    }

    /**
     * This method is called when the device is registered on Xiaomi Servers.
     *
     * @param context The [Context] where this code is run.
     * @param miPushCommandMessage A [MiPushCommandMessage] that contains inifo about the device.
     */
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
