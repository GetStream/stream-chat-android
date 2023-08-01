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

import io.getstream.chat.android.models.querysort.ComparableFieldProvider
import java.util.Date

/**
 * Represents a channel member.
 */
public data class Member(
    /**
     * The user who is a member of the channel.
     */
    override val user: User,

    /**
     * When the user became a member.
     */
    val createdAt: Date? = null,

    /**
     * When the membership data was last updated.
     */
    val updatedAt: Date? = null,

    /**
     * If the user is invited.
     */
    val isInvited: Boolean? = null,

    /**
     * The date the invite was accepted.
     */
    val inviteAcceptedAt: Date? = null,

    /**
     * The date the invite was rejected.
     */
    val inviteRejectedAt: Date? = null,

    /**
     * If channel member is shadow banned.
     */
    val shadowBanned: Boolean = false,

    /**
     * If channel member is banned.
     */
    val banned: Boolean = false,

    /**
     * The user's channel-level role.
     */
    val channelRole: String? = null,
) : UserEntity, ComparableFieldProvider {

    override fun getComparableField(fieldName: String): Comparable<*>? {
        return when (fieldName) {
            "userId" -> getUserId()
            "createdAt" -> createdAt
            "updatedAt" -> updatedAt
            "isInvited" -> isInvited
            "inviteAcceptedAt" -> inviteAcceptedAt
            "inviteRejectedAt" -> inviteRejectedAt
            "shadowBanned" -> shadowBanned
            "banned" -> banned
            "channelRole" -> channelRole
            else -> null
        }
    }
}
