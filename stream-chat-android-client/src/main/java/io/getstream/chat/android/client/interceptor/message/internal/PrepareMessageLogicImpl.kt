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

package io.getstream.chat.android.client.interceptor.message.internal

import io.getstream.chat.android.client.channel.state.ChannelStateLogicProvider
import io.getstream.chat.android.client.extensions.EXTRA_UPLOAD_ID
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.internal.populateMentions
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.interceptor.message.PrepareMessageLogic
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.internal.getMessageType
import io.getstream.chat.android.client.utils.message.ensureId
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import java.util.Date
import java.util.UUID

internal class PrepareMessageLogicImpl(
    private val clientState: ClientState,
    private val channelStateLogicProvider: ChannelStateLogicProvider?,
) : PrepareMessageLogic {

    /**
     * Prepares the message and its attachments but doesn't upload attachments.
     *
     * Following steps are required to initialize message properly before sending the message to the backend API:
     * 1. Message id is generated if the message doesn't have id.
     * 2. Message cid is updated if the message doesn't have cid.
     * 3. Message user is set to the current user.
     * 4. Attachments are prepared with upload state.
     * 5. Message timestamp and sync status is set.
     *
     * Then this message is inserted in database (Optimistic UI update) and final message is returned.
     */
    @Suppress("ComplexMethod")
    override fun prepareMessage(message: Message, channelId: String, channelType: String, user: User): Message {
        val channel = channelStateLogicProvider?.channelStateLogic(channelType, channelId)

        val attachments = message.attachments.map {
            when (it.upload) {
                null -> it.copy(uploadState = Attachment.UploadState.Success)
                else -> it.copy(
                    extraData = it.extraData + mapOf(EXTRA_UPLOAD_ID to (it.uploadId ?: generateUploadId())),
                    uploadState = Attachment.UploadState.Idle,
                )
            }
        }
        return message.ensureId(user).copy(
            user = user,
            attachments = attachments,
            type = getMessageType(message),
            createdLocallyAt = message.getCreatedAtOrDefault(Date()),
            syncStatus = when {
                attachments.any { it.uploadState is Attachment.UploadState.Idle } -> SyncStatus.AWAITING_ATTACHMENTS
                clientState.isNetworkAvailable -> SyncStatus.IN_PROGRESS
                else -> SyncStatus.SYNC_NEEDED
            },
        )
            .let { copiedMessage ->
                copiedMessage.takeIf { it.cid.isBlank() }
                    ?.enrichWithCid("$channelType:$channelId")
                    ?: copiedMessage
            }
            .let { copiedMessage ->
                channel
                    ?.listenForChannelState()
                    ?.toChannel()
                    ?.let(copiedMessage::populateMentions)
                    ?: copiedMessage
            }
            .also { preparedMessage ->
                if (preparedMessage.replyMessageId != null) {
                    channel?.setRepliedMessage(null)
                }
            }
    }

    private fun generateUploadId(): String {
        return "upload_id_${UUID.randomUUID()}"
    }
}
