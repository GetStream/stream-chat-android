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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.SendAttachmentListener
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry

/**
 * Updates the state of the SDK accordingly with request to send attachments to backend.
 */
internal class SendAttachmentListenerState(private val logic: LogicRegistry) : SendAttachmentListener {

    /**
     * Update the state of the SDK before the attachments are sent to the backend.
     *
     * @param channelType String
     * @param channelId String
     * @param message [Message]
     */
    override suspend fun onAttachmentSendRequest(channelType: String, channelId: String, message: Message) {
        val channel = logic.channel(channelType, channelId)

        channel.upsertMessage(message)
        logic.getActiveQueryThreadsLogic().forEach { it.upsertMessage(message) }
        logic.threadFromMessage(message)?.upsertMessage(message)

        // Update flow for currently running queries
        logic.getActiveQueryChannelsLogic().forEach { query -> query.refreshChannelState(channel.cid) }
    }
}
