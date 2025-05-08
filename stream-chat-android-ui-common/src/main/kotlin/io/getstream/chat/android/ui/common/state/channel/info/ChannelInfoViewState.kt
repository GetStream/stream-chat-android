/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.ExpandableList
import io.getstream.chat.android.ui.common.utils.emptyExpandableList

/**
 * Represents the state of the channel information in the UI.
 *
 * This sealed interface is used to model the different states that the channel information
 * can be in, such as loading or displaying content.
 */
@ExperimentalStreamChatApi
public sealed interface ChannelInfoViewState {

    /**
     * Represents the loading state of the channel information.
     */
    public data object Loading : ChannelInfoViewState

    /**
     * Represents the content state of the channel information.
     *
     * @param members The list of members in the channel.
     * @param name The name of the channel.
     * @param isMuted Indicates whether the channel is muted.
     * @param isHidden Indicates whether the channel is hidden.
     * @param capability The capabilities of the current user in the channel.
     */
    public data class Content(
        val members: ExpandableList<Member> = emptyExpandableList(),
        val name: String = "",
        val isMuted: Boolean = false,
        val isHidden: Boolean = false,
        val capability: Capability = Capability(),
    ) : ChannelInfoViewState {

        /**
         * Represents the capabilities of the current user in the channel.
         *
         * @param canAddMembers Indicates if the user can add members to the channel.
         * @param canRemoveMembers Indicates if the user can remove members from the channel.
         * @param canBanMembers Indicates if the user can ban members from the channel.
         * @param canRenameChannel Indicates if the user can rename the channel.
         * @param canMuteChannel Indicates if the user can mute the channel.
         * @param canLeaveChannel Indicates if the user can leave the channel.
         * @param canDeleteChannel Indicates if the user can delete the channel.
         */
        public data class Capability(
            val canAddMembers: Boolean = false,
            val canRemoveMembers: Boolean = false,
            val canBanMembers: Boolean = false,
            val canRenameChannel: Boolean = false,
            val canMuteChannel: Boolean = false,
            val canLeaveChannel: Boolean = false,
            val canDeleteChannel: Boolean = false,
        )

        /**
         * Represents a member of the channel.
         *
         * @param user The user object representing the member.
         * @param role The role of the member in the channel.
         */
        public data class Member(
            val user: User,
            val role: Role,
        )

        /**
         * Represents the role of a member in the channel.
         */
        public sealed interface Role {
            /**
             * Represents the owner role in the channel.
             */
            public data object Owner : Role

            /**
             * Represents the moderator role in the channel.
             */
            public data object Moderator : Role

            /**
             * Represents the member role in the channel.
             */
            public data object Member : Role

            /**
             * Represents other roles in the channel.
             *
             * @param value The string value representing the custom role.
             */
            public data class Other(val value: String) : Role
        }
    }
}
