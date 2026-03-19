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

package io.getstream.chat.android.ui.common.feature.channel.info

import io.getstream.chat.android.models.User

/**
 * Represents actions that can be performed from the "Add Members" view.
 */
public sealed interface AddMembersViewAction {

    /**
     * Represents a change in the search query.
     *
     * @param query The new search query.
     */
    public data class QueryChanged(val query: String) : AddMembersViewAction

    /**
     * Represents a click on a user in the search results.
     *
     * @param user The user that was clicked.
     */
    public data class UserClick(val user: User) : AddMembersViewAction

    /**
     * Represents a click on the confirm button to add the selected members.
     */
    public data object ConfirmClick : AddMembersViewAction
}
