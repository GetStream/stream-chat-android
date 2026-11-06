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

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.User

/**
 * Partially merges [that] user data into [this] user data if [that] is more up to date. Own-user fields, such as
 * mutes, are kept from [this], and so are privacy settings when [that] has none. When [that] has the same updatedAt
 * as [this], only its privacy settings are taken.
 */
@InternalStreamChatApi
public fun User.mergePartially(that: User): User = when {
    that.isMoreUpToDateThan(this) ->
        this.copy(
            role = that.role,
            createdAt = that.createdAt,
            updatedAt = that.updatedAt,
            lastActive = that.lastActive,
            online = that.online,
            banned = that.banned,
            name = that.name,
            image = that.image,
            // Stored privacy settings can't be cleared, so a payload without them just doesn't carry them
            privacySettings = that.privacySettings ?: privacySettings,
            extraData = that.extraData,
            language = that.language,
            teams = that.teams,
            teamsRole = that.teamsRole,
            deactivatedAt = that.deactivatedAt,
            avgResponseTime = that.avgResponseTime ?: avgResponseTime,
        )
    // Changing only the privacy settings doesn't move updatedAt
    that.updatedAt != null &&
        that.updatedAt == updatedAt &&
        that.privacySettings != null &&
        that.privacySettings != privacySettings ->
        this.copy(privacySettings = that.privacySettings)
    else -> this
}

private fun User.isMoreUpToDateThan(that: User): Boolean =
    (this.createdAt ?: NEVER) > (that.createdAt ?: NEVER) ||
        (this.updatedAt ?: NEVER) > (that.updatedAt ?: NEVER)
