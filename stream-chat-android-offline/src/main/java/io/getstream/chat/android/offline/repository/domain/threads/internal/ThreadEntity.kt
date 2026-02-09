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

package io.getstream.chat.android.offline.repository.domain.threads.internal

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.ChannelUserReadEntity
import java.util.Date

/**
 * Database entity for a Thread.
 */
@Suppress("LongParameterList")
@Entity(tableName = THREAD_ENTITY_TABLE_NAME)
internal data class ThreadEntity(
    @PrimaryKey val parentMessageId: String,
    val cid: String,
    val createdByUserId: String,
    val activeParticipantCount: Int,
    val participantCount: Int,
    val threadParticipants: List<ThreadParticipantEntity>,
    val lastMessageAt: Date,
    val createdAt: Date,
    val updatedAt: Date,
    val deletedAt: Date?,
    val title: String,
    val read: List<ChannelUserReadEntity>,
    val latestReplyIds: List<String>,
    val extraData: Map<String, Any>,
)

internal const val THREAD_ENTITY_TABLE_NAME = "stream_chat_thread"
