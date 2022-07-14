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
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import com.xiaomi.mipush.sdk.PushMessageReceiver
import io.getstream.logging.StreamLog

/**
 * Receiver that handle Push Notifications from Xiaomi servers.
 */
public class ChatXiaomiMessagingReceiver : PushMessageReceiver() {
    private val logger = StreamLog.getLogger("Chat:Notifications")

    /**
     * This method is called when a push notification is received from Xiaomi Servers.
     *
     * @param context The [Context] where this code is run.
     * @param miPushMessage A [MiPushMessage] that contains inifo about the push notification.
     */
    override fun onReceivePassThroughMessage(context: Context, miPushMessage: MiPushMessage) {
        logger.i { "[onReceiveXiaomiPassThroughMessage] miPushMessage: $miPushMessage" }
        try {
            XiaomiMessagingDelegate.handleMiPushMessage(miPushMessage)
        } catch (exception: IllegalStateException) {
            logger.e(exception) { "[onReceivePassThroughMessage] error while handling remote message" }
        }
    }

    /**
     * This method is called when the device is registered on Xiaomi Servers.
     *
     * @param context The [Context] where this code is run.
     * @param miPushCommandMessage A [MiPushCommandMessage] that contains inifo about the device.
     */
    override fun onReceiveRegisterResult(context: Context, miPushCommandMessage: MiPushCommandMessage) {
        logger.d { "[onReceiveXiaomiRegisterResult] miPushCommandMessage: $miPushCommandMessage" }
        try {
            XiaomiMessagingDelegate.registerXiaomiToken(miPushCommandMessage)
        } catch (exception: IllegalStateException) {
            logger.e(exception) { "[onReceiveRegisterResult] error while registering Xiaomi Token" }
        }
    }
}
