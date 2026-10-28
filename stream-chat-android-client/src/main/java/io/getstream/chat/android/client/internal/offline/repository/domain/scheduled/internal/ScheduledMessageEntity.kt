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

package io.getstream.chat.android.client.internal.offline.repository.domain.scheduled.internal

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.LocationEntity
import io.getstream.chat.android.models.UserGroup
import java.util.Date

/**
 * A scheduled message of the current user, with its content and the changes the server does not have yet. It has a
 * table of its own, so it never shows with the messages of a channel. The attachments are stored with their upload
 * state, as JSON.
 */
@Entity(tableName = SCHEDULED_MESSAGE_ENTITY_TABLE_NAME)
internal data class ScheduledMessageEntity(
    @PrimaryKey
    val id: String,
    val cid: String,
    val channelName: String?,
    val channelImage: String?,
    val userId: String,
    val text: String,
    val html: String,
    val type: String,
    val parentId: String?,
    val showInChannel: Boolean,
    val replyMessageId: String?,
    val attachments: List<AttachmentEntity>,
    val mentionedUsersIds: List<String>,
    val mentionedHere: Boolean,
    val mentionedChannel: Boolean,
    val mentionedGroups: List<UserGroup>,
    val mentionedRoles: List<String>,
    val pinned: Boolean,
    val pinExpires: Date?,
    val silent: Boolean,
    val restrictedVisibility: List<String>,
    val sharedLocation: LocationEntity?,
    val extraData: Map<String, Any>,
    val skipPushNotification: Boolean,
    val skipEnrichUrl: Boolean,
    val scheduledAt: Date,
    val serverStatus: String,
    val publishingFailureReason: String?,
    val hasPublishingFailure: Boolean,
    val pendingChange: String?,
    val hasPendingScheduledAt: Boolean,
    val isPendingSendNow: Boolean,
    val isDeleted: Boolean,
    val localFailureType: String?,
    val localFailureReason: String?,
    val localFailureMessage: String?,
    val createdAt: Date,
    val updatedAt: Date,
)

internal const val SCHEDULED_MESSAGE_ENTITY_TABLE_NAME = "stream_chat_scheduled_message"
