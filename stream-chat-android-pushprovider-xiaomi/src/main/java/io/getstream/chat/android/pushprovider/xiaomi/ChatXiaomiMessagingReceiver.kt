/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
