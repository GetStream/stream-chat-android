/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import java.util.Date

/**
 * Represents the unread counts for channels and threads in a chat application.
 *
 * @property messagesCount The total number of unread messages.
 * @property threadsCount The total number of unread threads.
 * @property messagesCountByTeam A map containing the count of unread messages by team ID.
 * @property channels A list of unread channels with their details.
 * @property threads A list of unread threads with their details.
 * @property channelsByType A list of unread channels grouped by type.
 */
public data class UnreadCounts(
    val messagesCount: Int = 0,
    val threadsCount: Int = 0,
    val messagesCountByTeam: Map<String, Int> = emptyMap(),
    val channels: List<UnreadChannel> = emptyList(),
    val threads: List<UnreadThread> = emptyList(),
    val channelsByType: List<UnreadChannelByType> = emptyList(),
)

/**
 * Represents an unread channel.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param messagesCount The number of unread messages in the channel.
 * @param lastRead The date when the last message was read in the channel.
 */
public data class UnreadChannel(
    val cid: String,
    val messagesCount: Int,
    val lastRead: Date,
)

/**
 * Represents an unread thread.
 *
 * @property parentMessageId The ID of the parent message in the thread.
 * @property messagesCount The number of unread messages in the thread.
 * @property lastRead The date when the last message was read in the thread.
 * @property lastReadMessageId The ID of the last read message in the thread.
 */
public data class UnreadThread(
    val parentMessageId: String,
    val messagesCount: Int,
    val lastRead: Date,
    val lastReadMessageId: String,
)

/**
 * Represents the count of unread channels grouped by their type.
 *
 * @property channelType The type of the channel (e.g., "messaging", "livestream").
 * @property channelsCount The number of unread channels of this type.
 * @property messagesCount The total number of unread messages in these channels.
 */
public data class UnreadChannelByType(
    val channelType: String,
    val channelsCount: Int,
    val messagesCount: Int,
)
