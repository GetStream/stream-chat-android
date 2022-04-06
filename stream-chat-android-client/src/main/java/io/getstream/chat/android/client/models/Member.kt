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
 
package io.getstream.chat.android.client.models

import java.util.Date

/**
 * Represents a channel member.
 */
public data class Member(
    /**
     * The user who is a member of the channel.
     */
    override var user: User,

    /**
     * The user's role.
     */
    @Deprecated(
        level = DeprecationLevel.ERROR,
        message = "Use channelRole instead."
    )
    var role: String? = null,

    /**
     * When the user became a member.
     */
    var createdAt: Date? = null,

    /**
     * When the membership data was last updated.
     */
    var updatedAt: Date? = null,

    /**
     * If the user is invited.
     */
    var isInvited: Boolean? = null,

    /**
     * The date the invite was accepted.
     */
    var inviteAcceptedAt: Date? = null,

    /**
     * The date the invite was rejected.
     */
    var inviteRejectedAt: Date? = null,

    /**
     * If channel member is shadow banned.
     */
    var shadowBanned: Boolean = false,

    /**
     * If channel member is banned.
     */
    var banned: Boolean = false,

    /**
     * The user's channel-level role.
     */
    var channelRole: String? = null,
) : UserEntity
