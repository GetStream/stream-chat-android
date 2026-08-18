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
 * Model holding limited data about a channel member, as attached to a [Message] by [Message.member].
 *
 * This is not a full [Member]: there is no user, no timestamps and no invite or ban state.
 */
@Immutable
public data class MemberInfo(
    /**
     * The channel-level role of the member.
     */
    val channelRole: String? = null,

    /**
     * If notifications are muted for the member in the channel.
     */
    val notificationsMuted: Boolean = false,

    /**
     * A map of custom fields for the member.
     *
     * Only populated when the app has member custom data on messages enabled.
     */
    override val extraData: Map<String, Any> = emptyMap(),
) : CustomObject
