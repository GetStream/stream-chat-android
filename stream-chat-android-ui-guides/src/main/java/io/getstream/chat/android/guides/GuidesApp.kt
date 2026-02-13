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

package io.getstream.chat.android.guides

import android.app.Application
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.StateConfig
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.UploadAttachmentsNetworkType

class GuidesApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val stateConfig = StateConfig(
            backgroundSyncEnabled = false,
            userPresence = true,
        )

        ChatClient.Builder("qx5us2v6xvmh", this)
            .stateConfig(stateConfig)
            .uploadAttachmentsNetworkType(UploadAttachmentsNetworkType.NOT_ROAMING)
            .logLevel(ChatLogLevel.NOTHING)
            .build()
    }
}
