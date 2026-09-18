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

/**
 * Model representing a typing event.
 *
 * @param channelId The ID of the channel where the typing event occurred.
 * @param users The users who are currently typing in the channel.
 * @param typingUsers The users who are currently typing in the channel, with their channel membership. Defaults to
 * [users] without membership, so both lists describe the same people however the event is constructed. The default is
 * evaluated at construction, so `copy(users = ...)` keeps the receiver's [typingUsers] and the two can then disagree.
 */
@Immutable
public data class TypingEvent(
    val channelId: String,
    @Deprecated(
        message = "Use typingUsers instead, which also carries each user's channel membership.",
        replaceWith = ReplaceWith("typingUsers"),
        level = DeprecationLevel.WARNING,
    )
    val users: List<User>,
    val typingUsers: List<TypingUser> = users.map(::TypingUser),
)
