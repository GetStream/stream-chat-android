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
 * A message of the current user that the server sends to a channel at [scheduledAt]. Until it is sent, it can be
 * edited, rescheduled, sent right away or deleted.
 *
 * @property id The ID of the scheduled message. It is also the ID of [message], and not the ID of the message the
 * server creates when it sends it.
 * @property cid The ID of the channel the message is sent to, in the format `type:id`.
 * @property channel A summary of the channel the message is sent to. The server sends it only with the results of a
 * scheduled messages query, and the scheduled messages kept on the device keep the last one received, so it can be
 * null.
 * @property message The content that will be sent. It is not in the messages of the channel until it is sent, and has
 * no reactions, replies or read state. Its [Message.createdAt] is [scheduledAt], so it can be shown like any other
 * message, and its [Message.updatedAt] is the one of the scheduled message. The mentioned users and groups, the
 * quoted message and the poll come as IDs only: in [Message.mentionedUsersIds], [Message.mentionedGroups] (without
 * names), [Message.replyMessageId] and the `poll_id` of [Message.extraData].
 * @property scheduledAt The date when the server sends the message.
 * @property status Where the scheduled message is, from scheduling until it is sent.
 * @property failure Why the message could not be scheduled or sent, or null when nothing failed.
 * @property createdAt The date when the message was scheduled.
 * @property updatedAt The date when the message was last edited or rescheduled.
 */
@Immutable
public data class ScheduledMessage(
    val id: String,
    val cid: String,
    val channel: ChannelInfo?,
    val message: Message,
    val scheduledAt: Date,
    val status: ScheduledMessageStatus,
    val failure: ScheduledMessageFailure?,
    val createdAt: Date,
    val updatedAt: Date,
)

/**
 * The result of a scheduled messages query.
 *
 * @property scheduledMessages The scheduled messages of the page: the pending ones first, then the failed ones, each
 * sorted by [ScheduledMessage.scheduledAt].
 * @property next The cursor of the next page, or null on the last page.
 */
@Immutable
public data class QueryScheduledMessagesResult(
    val scheduledMessages: List<ScheduledMessage>,
    val next: String?,
)
