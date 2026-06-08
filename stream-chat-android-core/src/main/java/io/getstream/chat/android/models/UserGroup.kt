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
import java.util.Date

/**
 * Named collection of users that can be mentioned together with `@<name>`.
 *
 * @param id Unique identifier of the group within an app (or team, when multi-tenancy is enabled).
 * @param name Display name; the literal that follows `@` in message text.
 * @param description Optional description.
 * @param team Team this group is scoped to, when multi-tenancy is enabled. Empty when not.
 * @param members Group members. May be empty when the group object only carries metadata.
 * @param createdBy User ID of the creator, when known.
 * @param createdAt When the group was created.
 * @param updatedAt Last time the group's metadata changed.
 */
@Immutable
public data class UserGroup(
    val id: String,
    val name: String,
    val description: String? = null,
    val team: String = "",
    val members: List<UserGroupMember> = emptyList(),
    val createdBy: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
)

/**
 * Membership of a [UserGroup].
 *
 * @param groupId The group this membership belongs to.
 * @param userId The user that is a member of the group.
 * @param isAdmin Whether the user is an admin of the group.
 * @param createdAt When the user was added to the group.
 */
@Immutable
public data class UserGroupMember(
    val groupId: String,
    val userId: String,
    val isAdmin: Boolean = false,
    val createdAt: Date? = null,
)
