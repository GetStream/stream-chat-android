/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import java.util.Date

@Immutable
public data class Thread(
    val activeParticipantCount: Int,
    val cid: String,
    val channel: Channel?,
    val parentMessageId: String,
    val parentMessage: Message,
    val createdByUserId: String,
    val createdBy: User?,
    val replyCount: Int,
    val participantCount: Int,
    val threadParticipants: List<User>,
    val lastMessageAt: Date,
    val createdAt: Date,
    val updatedAt: Date?,
    val deletedAt: Date?,
    val title: String,
    val latestReplies: List<Message>,
    val read: List<ChannelUserRead>,
)
