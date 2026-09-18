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
 * A user currently typing in a channel, together with their membership of that channel.
 *
 * The membership is kept beside [user] rather than on it, because [User] is shared across every channel while
 * [MemberInfo] describes this one channel only.
 */
@Immutable
public data class TypingUser(
    /**
     * The user who is typing.
     */
    val user: User,

    /**
     * Data about the channel membership of the user who is typing.
     *
     * Only populated when the app has member custom data on messages enabled.
     */
    val member: MemberInfo? = null,
)
