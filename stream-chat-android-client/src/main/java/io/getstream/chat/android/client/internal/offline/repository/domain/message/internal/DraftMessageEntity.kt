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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

@Entity(
    tableName = DRAFT_MESSAGE_ENTITY_TABLE_NAME,
)
internal data class DraftMessageEntity(
    @PrimaryKey
    val id: String,
    val cid: String,
    val text: String,
    val parentId: String? = null,
    val attachments: List<AttachmentEntity> = listOf(),
    val mentionedUsersIds: List<String> = listOf(),
    val silent: Boolean,
    val showInChannel: Boolean,
    val replyMessageId: String? = null,
    val command: String? = null,
    val args: String? = null,
    val extraData: Map<String, Any> = mapOf(),
    val createdAt: Date? = null,
    /** [SyncStatus.SYNC_NEEDED] while a local change of this draft has not reached the server yet. */
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,
    /** `true` for a draft deleted locally: the row stays hidden until the deletion reaches the server. */
    val deletedLocally: Boolean = false,
)

internal const val DRAFT_MESSAGE_ENTITY_TABLE_NAME = "stream_chat_draft_message_entity"
