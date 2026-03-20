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

package io.getstream.chat.android.ui.common.state.channel.info

import io.getstream.chat.android.models.User

/**
 * Represents the state of the "Add Members" view.
 *
 * @param isLoading Whether the view is currently loading the initial search results.
 * @param isLoadingMore Whether the view is currently loading additional (paginated) results.
 * @param query The current search query.
 * @param searchResult The list of users matching the search query.
 * @param selectedUserIds The set of IDs of users selected to be added as members.
 * @param loadedMemberIds The set of IDs of users who are already members of the channel.
 */
public data class AddMembersViewState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val query: String = "",
    val searchResult: List<User> = emptyList(),
    val selectedUserIds: Set<String> = emptySet(),
    val loadedMemberIds: Set<String> = emptySet(),
) {
    /**
     * The list of users selected to be added as members, derived from [searchResult] and [selectedUserIds].
     */
    public val selectedUsers: List<User> get() = searchResult.filter { it.id in selectedUserIds }

    /**
     * Returns true if the given [user] is selected to be added as a member.
     */
    public fun isSelected(user: User): Boolean = user.id in selectedUserIds

    /**
     * Returns true if the given [user] is already a member of the channel.
     */
    public fun isAlreadyMember(user: User): Boolean = user.id in loadedMemberIds
}
