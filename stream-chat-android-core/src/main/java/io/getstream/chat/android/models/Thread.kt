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

/**
 * Domain model for a thread. Holds all information related to a thread.
 *
 * @param activeParticipantCount The number of active participants in the thread.
 * @param cid Id of the channel in which the thread resides.
 * @param channel The [Channel] object holding info about the channel if which the thread resides.
 * @param parentMessageId The ID of the parent message of the thread.
 * @param parentMessage The parent message of the thread. (Note: This object is not always delivered, sometimes we only
 * receive the ID of the parent message - [parentMessageId]).
 * @param createdByUserId The ID of the [User] which created the thread.
 * @param createdBy The [User] which created the thread. (Note: This object is not always delivered, sometimes we only
 * receive the ID of the user - [createdByUserId]).
 * @param participantCount The number of participants in the thread.
 * @param threadParticipants The list of participants in the thread.
 * @param lastMessageAt Date of the last message in the thread.
 * @param createdAt Date when the thread was created.
 * @param updatedAt Date of the most recent update of the thread.
 * @param deletedAt Date when the thread was deleted (null if the thread is not deleted).
 * @param title The title of the thread.
 * @param latestReplies The list of latest replies in the thread.
 * @param read Information about the read status for the participants in the thread.
 * @param draftMessage The draft message in the thread, if any.
 */
@Immutable
public data class Thread(
    val activeParticipantCount: Int,
    val cid: String,
    val channel: Channel?,
    val parentMessageId: String,
    val parentMessage: Message,
    val createdByUserId: String,
    val createdBy: User?,
    val participantCount: Int,
    val threadParticipants: List<ThreadParticipant>,
    val lastMessageAt: Date,
    val createdAt: Date,
    val updatedAt: Date,
    val deletedAt: Date?,
    val title: String,
    val latestReplies: List<Message>,
    val read: List<ChannelUserRead>,
    val draftMessage: DraftMessage?,
) {

    /**
     * The number of replies in the thread (replies to the parent message).
     */
    val replyCount: Int
        get() = parentMessage.replyCount
}
