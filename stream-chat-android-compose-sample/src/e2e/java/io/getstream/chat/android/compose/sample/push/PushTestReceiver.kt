/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import io.getstream.android.push.firebase.FirebaseMessagingDelegate

/**
 * Feeds a push payload into the production push pipeline during E2E tests. The mock server
 * delivers the payload as an adb broadcast; every string extra becomes an entry of the
 * [RemoteMessage] data map, which then goes through the same validation and rendering path
 * as a real FCM message.
 */
internal class PushTestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras ?: return
        val data = extras.keySet()
            .mapNotNull { key -> extras.getString(key)?.let { value -> key to value } }
            .toMap()
        FirebaseMessagingDelegate.handleRemoteMessage(RemoteMessage.Builder(SENDER).setData(data).build())
    }

    private companion object {
        private const val SENDER = "test@fcm.googleapis.com"
    }
}
