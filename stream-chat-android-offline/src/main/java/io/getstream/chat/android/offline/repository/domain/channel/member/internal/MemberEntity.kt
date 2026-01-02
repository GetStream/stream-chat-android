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

package io.getstream.chat.android.offline.repository.domain.channel.member.internal

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Efficiently store the member data
 */
@JsonClass(generateAdapter = true)
internal data class MemberEntity(
    var userId: String,
    /** the user's role, user, moderator or admin */
    var role: String = "",

    /** when the user became a member */
    var createdAt: Date? = null,
    /** when the membership data was last updated */
    var updatedAt: Date? = null,

    /** if this is an invite */
    var isInvited: Boolean = false,

    /** the date the invite was accepted */
    var inviteAcceptedAt: Date? = null,

    /** the date the invite was rejected */
    var inviteRejectedAt: Date? = null,

    /** if channel member is shadow banned */
    var shadowBanned: Boolean = false,

    /** If channel member is banned. */
    var banned: Boolean = false,

    /** The user's channel-level role. */
    val channelRole: String? = null,

    /** If the notifications are muted for this user in the channel. */
    val notificationsMuted: Boolean? = null,

    /** The user's status. */
    val status: String? = null,

    /** The date the ban expires. */
    var banExpires: Date? = null,

    /** The date when the member pinned the channel. */
    val pinnedAt: Date? = null,

    /** The date when the member archived the channel. */
    val archivedAt: Date? = null,

    /** Map of custom fields for the member. */
    val extraData: Map<String, Any> = emptyMap(),
)
