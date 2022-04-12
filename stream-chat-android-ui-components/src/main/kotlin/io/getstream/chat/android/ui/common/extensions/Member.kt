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

@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.ui.common.extensions

import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.ui.common.extensions.internal.isCurrentUser

/**
 * @return if the member is the owner or an admin of a channel.
 */
public val Member.isOwnerOrAdmin: Boolean
    get() = role == "owner" || role == "admin"

/**
 * @return if the current user is an owner or an admin of a channel.
 */
public fun List<Member>?.isCurrentUserOwnerOrAdmin(): Boolean {
    return if (isNullOrEmpty()) {
        false
    } else {
        firstOrNull { it.user.isCurrentUser() }?.isOwnerOrAdmin ?: false
    }
}
