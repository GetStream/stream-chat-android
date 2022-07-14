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

package io.getstream.chat.android.pushprovider.huawei

import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import io.getstream.logging.StreamLog

internal class ChatHuaweiMessagingService : HmsMessageService() {
    private val logger = StreamLog.getLogger("Chat:Notifications")

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.d { "[onHuaweiMessageReceived] remoteMessage: $remoteMessage" }
        try {
            HuaweiMessagingDelegate.handleRemoteMessage(remoteMessage)
        } catch (exception: IllegalStateException) {
            logger.e(exception) { "[onHuaweiMessageReceived] error while handling remote message" }
        } finally {
            stopSelf()
        }
    }

    override fun onNewToken(token: String) {
        try {
            HuaweiMessagingDelegate.registerHuaweiToken(token)
        } catch (exception: IllegalStateException) {
            logger.e(exception) { "[onHuaweiNewToken] error while registering Huawei Token" }
        }
    }
}
