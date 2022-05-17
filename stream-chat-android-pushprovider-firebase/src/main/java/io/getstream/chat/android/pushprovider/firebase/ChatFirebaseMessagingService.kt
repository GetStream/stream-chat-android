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

package io.getstream.chat.android.pushprovider.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.logger.ChatLogger

internal class ChatFirebaseMessagingService : FirebaseMessagingService() {
    private val logger = ChatLogger.get("ChatFirebaseMessagingService")

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.logD("onMessageReceived(): $remoteMessage")
        try {
            FirebaseMessagingDelegate.handleRemoteMessage(remoteMessage)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while handling remote message", exception)
        } finally {
            stopSelf()
        }
    }

    override fun onNewToken(token: String) {
        try {
            FirebaseMessagingDelegate.registerFirebaseToken(token, null)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while registering Firebase Token", exception)
        }
    }

    private companion object {
        private const val TAG = "Chat:"
    }
}
