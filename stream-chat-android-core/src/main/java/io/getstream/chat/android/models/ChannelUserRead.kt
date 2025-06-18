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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import java.util.Date

/**
 * Information about how many messages are unread in the channel by a given user.
 *
 * @property user The user which has read some of the messages and may have some unread messages.
 * @property lastReceivedEventDate The time of the event that updated this [ChannelUserRead] object.
 * @property lastRead The time of the last read message.
 * @property unreadMessages How many messages are unread.
 * @property unreadThreads How many threads are unread. Optional and may be null.
 * @property lastReadMessageId The ID of the last read message.
 */
@Immutable
public data class ChannelUserRead(
    override val user: User,
    val lastReceivedEventDate: Date,
    val unreadMessages: Int,
    val unreadThreads: Int? = null,
    val lastRead: Date,
    val lastReadMessageId: String?,
) : UserEntity
