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

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.ReplyAttachmentEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.channelinfo.internal.ChannelInfoEntity
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

internal data class ReplyMessageEntity(
    @Embedded val replyMessageInnerEntity: ReplyMessageInnerEntity,
    @Relation(entity = ReplyAttachmentEntity::class, parentColumn = "id", entityColumn = "messageId")
    val attachments: List<ReplyAttachmentEntity>,
)

@Entity(
    tableName = REPLY_MESSAGE_ENTITY_TABLE_NAME,
    indices = [
        Index(value = ["cid", "createdAt"]),
        Index(value = ["syncStatus"]),
    ],
)
internal data class ReplyMessageInnerEntity(
    @PrimaryKey
    val id: String,
    val cid: String,
    val userId: String,
    /** the message text */
    val text: String = "",
    /** the message text formatted as html **/
    val html: String = "",
    /** message type can be system, regular or ephemeral */
    val type: String = "",
    /** if the message has been synced to the servers, default is synced */
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,
    /** the number of replies */
    val replyCount: Int = 0,
    /** the number of deleted replies */
    val deletedReplyCount: Int = 0,
    /** when the message was created */
    val createdAt: Date? = null,
    /** when the message was created locally */
    val createdLocallyAt: Date? = null,
    /** when the message was updated */
    val updatedAt: Date? = null,
    /** when the message was updated locally */
    val updatedLocallyAt: Date? = null,
    /** when the message was deleted */
    val deletedAt: Date? = null,
    /** the users mentioned in this message */
    val remoteMentionedUserIds: List<String> = emptyList(),
    /** the users to be mentioned in this message */
    val mentionedUsersId: List<String> = emptyList(),
    /** parent id, used for threads */
    val parentId: String? = null,
    /** slash command like /giphy etc */
    val command: String? = null,
    /** if the message was sent by shadow banned user */
    val shadowed: Boolean = false,
    /** message internationalization mapping.*/
    val i18n: Map<String, String> = emptyMap(),
    /** if the message is also shown in the channel **/
    val showInChannel: Boolean = false,
    @Embedded(prefix = "channel_info")
    val channelInfo: ChannelInfoEntity? = null,
    /** if the message is silent  **/
    val silent: Boolean = false,
    /** all the custom data provided for this message */
    val extraData: Map<String, Any> = emptyMap(),
    /** whether message is pinned or not **/
    val pinned: Boolean,
    /** date when the message got pinned **/
    val pinnedAt: Date? = null,
    /** date when pinned message expires **/
    val pinExpires: Date? = null,
    /** the ID of the user who pinned the message **/
    val pinnedByUserId: String?,
    /** participants of thread replies */
    val threadParticipantsIds: List<String> = emptyList(),
    /** Contains moderation details of the message **/
    val moderationDetails: ModerationDetailsEntity? = null,
    /** Date when the message text was updated **/
    val messageTextUpdatedAt: Date? = null,
    /** The ID of the poll **/
    val pollId: String?,
    /** The list of user IDs that have restricted visibility of the message **/
    val restrictedVisibility: List<String> = emptyList(),
    /** Info about the reminder for the message **/
    val reminder: ReminderInfoEntity? = null,
    /** The role of the member(who sent the message) in the channel */
    val channelRole: String? = null,
)

internal const val REPLY_MESSAGE_ENTITY_TABLE_NAME = "stream_chat_reply_message"
