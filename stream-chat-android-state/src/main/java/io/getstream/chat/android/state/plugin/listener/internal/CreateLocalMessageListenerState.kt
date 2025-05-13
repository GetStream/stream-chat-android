/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.CreateLocalMessageListener
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry

@OptIn(ExperimentalStreamChatApi::class)
internal class CreateLocalMessageListenerState(private val logic: LogicRegistry) : CreateLocalMessageListener {

    override suspend fun onCreateLocalMessageRequest(message: Message) {
        logic.channelFromMessage(message)?.upsertMessage(message)
        logic.threads().upsertMessage(message)
        logic.threadFromMessage(message)?.upsertMessage(message)
    }
}
