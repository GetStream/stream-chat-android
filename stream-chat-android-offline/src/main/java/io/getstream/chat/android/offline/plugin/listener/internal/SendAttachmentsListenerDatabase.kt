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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.plugin.listeners.SendAttachmentListener

internal class SendAttachmentsListenerDatabase(
    private val messageRepository: MessageRepository,
    private val channelRepository: ChannelRepository,
) : SendAttachmentListener {

    override suspend fun onAttachmentSendRequest(channelType: String, channelId: String, message: Message) {
        // we insert early to ensure we don't lose messages
        messageRepository.insertMessage(message)
        channelRepository.updateLastMessageForChannel(message.cid, message)
    }
}
