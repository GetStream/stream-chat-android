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

import java.util.Date

/**
 * Information about how many messages are unread in the channel by a given user.
 *
 * @property user The user which has read some of the messages and may have some unread messages.
 * @property lastRead The time of the last read message.
 * @property unreadMessages How many messages are unread.
 * @property lastMessageSeenDate The time of the last message that the SDK is aware of. If new messages arrive with
 * the createdAt newer than this one, that means that the count of unread messages should be incremented.
 */
public data class ChannelUserRead(
    override var user: User,
    var lastRead: Date? = null,
    var unreadMessages: Int = 0,
    var lastMessageSeenDate: Date? = null
) : UserEntity
