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

package io.getstream.chat.android.uitests.app

import android.app.Application
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.StateConfig
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory

class ChatApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = this)
        val stateConfig = StateConfig(
            backgroundSyncEnabled = false,
            userPresence = true,
        )

        ChatClient.Builder("hrwwzsgrzapv", this)
            .withPlugins(offlinePluginFactory)
            .stateConfig(stateConfig)
            .logLevel(ChatLogLevel.NOTHING)
            .uploadAttachmentsNetworkType(UploadAttachmentsNetworkType.NOT_ROAMING)
            .build()
    }
}
