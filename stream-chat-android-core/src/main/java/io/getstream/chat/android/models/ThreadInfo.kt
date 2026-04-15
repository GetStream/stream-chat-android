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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import java.util.Date

/**
 * Domain model for thread info. Holds partial information related to a thread.
 *
 * @param activeParticipantCount Number of active participants in the thread.
 * @param cid Id of the channel in which the thread resides.
 * @param createdAt Date when the thread was created.
 * @param createdBy The [User] which created the thread. (Note: This object is not always delivered, sometimes we only
 * receive the ID of the user - [createdByUserId]).
 * @param createdByUserId The ID of the [User] which created the thread.
 * @param deletedAt Date when the thread was deleted (null if the thread is not deleted).
 * @param lastMessageAt Date of the last message in the thread.
 * @param parentMessage The parent message of the thread. (Note: This object is not always delivered, sometimes we only
 * receive the ID of the parent message - [parentMessageId]).
 * @param parentMessageId The ID of the parent message of the thread.
 * @param participantCount The number of participants in the thread.
 * @param replyCount The number of replies in the thread.
 * @param title The title of the thread.
 * @param updatedAt Date of the most recent update of the thread.
 * @param channel The [Channel] object holding info about the channel in which the thread resides.
 * @param threadParticipants The list of participants in the thread.
 * @param extraData Any additional data.
 */
@Immutable
public data class ThreadInfo(
    val activeParticipantCount: Int,
    val cid: String,
    val createdAt: Date,
    val createdBy: User?,
    val createdByUserId: String,
    val deletedAt: Date?,
    val lastMessageAt: Date?,
    val parentMessage: Message?,
    val parentMessageId: String,
    val participantCount: Int,
    val replyCount: Int,
    val title: String,
    val updatedAt: Date,
    val channel: Channel? = null,
    val threadParticipants: List<ThreadParticipant> = emptyList(),
    override val extraData: Map<String, Any> = emptyMap(),
) : CustomObject
